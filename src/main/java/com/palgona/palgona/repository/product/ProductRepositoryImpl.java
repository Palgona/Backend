package com.palgona.palgona.repository.product;

import static com.palgona.palgona.domain.bidding.QBidding.bidding;
import static com.palgona.palgona.domain.bookmark.QBookmark.bookmark;
import static com.palgona.palgona.domain.image.QImage.image;
import static com.palgona.palgona.domain.product.QProduct.product;
import static com.palgona.palgona.domain.product.QProductImage.productImage;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.SortType;
import com.palgona.palgona.dto.response.ProductPageResponse;
import com.palgona.palgona.repository.product.querydto.ImageQueryResponse;
import com.palgona.palgona.repository.product.querydto.ProductQueryResponse;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private static final int MAX_PRICE_DIGIT = 9;
    private static final int MAX_ID_DIGIT = 8;
    private static final int MAX_BOOKMARK_DIGIT = 6;

    private final JPAQueryFactory queryFactory;

    @Override
    public SliceResponse<ProductPageResponse> findAllByCategoryAndSearchWord(
            Category category,
            String searchWord,
            String cursor,
            SortType sortType,
            int pageSize) {

        List<ProductQueryResponse> productQueryResponses = queryFactory.select(Projections.constructor(
                ProductQueryResponse.class,
                        product.id,
                        product.name,
                        bidding.price.max().coalesce(0),
                        bookmark.countDistinct().intValue().coalesce(0),
                        product.deadline,
                        product.createdAt
                ))
                .from(product)
                .leftJoin(bidding).on(bidding.product.id.eq(product.id))
                .leftJoin(bookmark).on(bookmark.product.id.eq(product.id))
                .where(contains(searchWord), categoryEq(category))
                .groupBy(product.id)
                .having(isInSearchRange(cursor, sortType))
                .orderBy(createOrderSpecifier(sortType))
                .limit(pageSize + 1)
                .fetch();

        List<ImageQueryResponse> imageQueryResponses = queryFactory.select(Projections.constructor(
                ImageQueryResponse.class,
                        product.id,
                        image.imageId,
                        image.imageUrl))
                .from(productImage)
                .join(productImage.image, image)
                .where(productImage.product.id.in(toProductIds(productQueryResponses)))
                .fetch();

        Map<Long, List<ImageQueryResponse>> result = imageQueryResponses.stream()
                .collect(Collectors.groupingBy(ImageQueryResponse::productId));

        List<ProductPageResponse> productResponses = productQueryResponses.stream()
                .map(response -> {
                    List<ImageQueryResponse> images = result.get(response.id());
                    Long minId = Long.MAX_VALUE;
                    String imageUrl = null;
                    for (ImageQueryResponse image : images) {
                        if (minId > image.imageId()) {
                            minId = image.imageId();
                            imageUrl = image.imageUrl();
                        }
                    }

                    return ProductPageResponse.of(response, imageUrl);
                }).collect(Collectors.toList());

        return convertToSlice(productResponses, sortType, pageSize);
    }

    private List<Long> toProductIds(List<ProductQueryResponse> productResponses) {
        return productResponses.stream()
                .map(ProductQueryResponse::id)
                .toList();
    }

    private BooleanExpression categoryEq(Category category) {
        if (category != null) {
            return product.category.eq(category);
        }

        return null;
    }

    private BooleanExpression contains(String searchWord) {
        if (searchWord != null) {
            return product.name.like("%" + searchWord + "%");
        }

        return null;
    }

    private SliceResponse<ProductPageResponse> convertToSlice(
            List<ProductPageResponse> products,
            SortType sortType,
            int pageSize) {
        if (products.isEmpty()) {
            return SliceResponse.of(products, false, null);
        }

        boolean hasNext = existNextPage(products, pageSize);
        if (hasNext) {
            deleteLastPage(products, pageSize);
        }

        String nextCursor = generateCursor(products, sortType);
        return SliceResponse.of(products, hasNext, nextCursor);
    }

    private String generateCursor(List<ProductPageResponse> products, SortType sortType) {
        ProductPageResponse lastProduct = products.get(products.size() - 1);

        return switch (sortType) {
            case DEADLINE -> String.valueOf(lastProduct.deadline());
            case HIGHEST_PRICE, LOWEST_PRICE -> String.format("%09d", lastProduct.currentBid())
                    + String.format("%08d", lastProduct.id());
            case BOOK_MARK -> String.format("%06d", lastProduct.bookmarkCount())
                    + String.format("%08d", lastProduct.id());
            default -> String.valueOf(lastProduct.id());
        };
    }

    private boolean existNextPage(List<ProductPageResponse> products, int pageSize) {
        if (products.size() > pageSize){
            return true;
        }
        return false;
    }

    private void deleteLastPage(List<ProductPageResponse> products, int pageSize) {
        products.remove(pageSize);
    }

    private BooleanExpression isInSearchRange(String cursor, SortType sortType) {
        if (cursor == null) {
            return null;
        }

        return switch (sortType) {
            case DEADLINE -> product.deadline.before(LocalDateTime.parse(cursor, DateTimeFormatter.ISO_DATE_TIME));
            case HIGHEST_PRICE -> StringExpressions.lpad(bidding.price.max().stringValue(), MAX_PRICE_DIGIT, '0')
                    .concat(StringExpressions.lpad(product.id.stringValue(), MAX_ID_DIGIT, '0'))
                    .lt(cursor);
            case LOWEST_PRICE -> StringExpressions.lpad(bidding.price.max().stringValue(), MAX_PRICE_DIGIT, '0')
                    .concat(StringExpressions.lpad(product.id.stringValue(), MAX_ID_DIGIT, '0'))
                    .gt(cursor);
            case BOOK_MARK -> StringExpressions.lpad(bookmark.countDistinct().stringValue(), MAX_BOOKMARK_DIGIT, '0')
                    .concat(StringExpressions.lpad(product.id.stringValue(), MAX_ID_DIGIT, '0'))
                    .lt(cursor);
            default -> product.id.lt(Long.valueOf(cursor));
        };
    }

    private OrderSpecifier createOrderSpecifier(SortType sortType) {
        return switch (sortType) {
            case DEADLINE -> new OrderSpecifier<>(Order.DESC, product.deadline);
            case HIGHEST_PRICE -> new OrderSpecifier<>(Order.DESC, bidding.price.max());
            case LOWEST_PRICE -> new OrderSpecifier<>(Order.ASC, bidding.price.max());
            case BOOK_MARK -> new OrderSpecifier<>(Order.DESC, bookmark.countDistinct());
            default -> new OrderSpecifier<>(Order.DESC, product.id);
        };
    }
}
