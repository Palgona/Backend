package com.palgona.palgona.repository.product;

import static com.palgona.palgona.domain.product.QProduct.product;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.SortType;
import com.palgona.palgona.dto.response.ProductPageResponse;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

        List<Product> products = queryFactory.selectFrom(product)
                .where(contains(searchWord), categoryEq(category), isInSearchRange(cursor, sortType))
                .orderBy(createOrderSpecifier(sortType))
                .limit(pageSize + 1)
                .fetch();

        List<ProductPageResponse> productResponses = products.stream()
                .map(ProductPageResponse::from)
                .collect(Collectors.toList());

        return convertToSlice(productResponses, sortType, pageSize);
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
        if (products.size() > pageSize) {
            products.remove(pageSize);
            return true;
        }

        return false;
    }

    private BooleanExpression isInSearchRange(String cursor, SortType sortType) {
        if (cursor == null) {
            return null;
        }

        return switch (sortType) {
            case DEADLINE -> product.deadline.before(LocalDateTime.parse(cursor, DateTimeFormatter.ISO_DATE_TIME));
            case HIGHEST_PRICE -> StringExpressions.lpad(product.currentBid.stringValue(), MAX_PRICE_DIGIT, '0')
                    .concat(StringExpressions.lpad(product.id.stringValue(), MAX_ID_DIGIT, '0'))
                    .lt(cursor);
            case LOWEST_PRICE -> StringExpressions.lpad(product.currentBid.stringValue(), MAX_PRICE_DIGIT, '0')
                    .concat(StringExpressions.lpad(product.id.stringValue(), MAX_ID_DIGIT, '0'))
                    .gt(cursor);
            case BOOK_MARK -> StringExpressions.lpad(product.bookmarkCount.stringValue(), MAX_BOOKMARK_DIGIT, '0')
                    .concat(StringExpressions.lpad(product.id.stringValue(), MAX_BOOKMARK_DIGIT, '0'))
                    .lt(cursor);
            default -> product.id.lt(Long.valueOf(cursor));
        };
    }

    private OrderSpecifier createOrderSpecifier(SortType sortType) {
        return switch (sortType) {
            case DEADLINE -> new OrderSpecifier<>(Order.DESC, product.deadline);
            case HIGHEST_PRICE -> new OrderSpecifier<>(Order.DESC, product.currentBid);
            case LOWEST_PRICE -> new OrderSpecifier<>(Order.ASC, product.currentBid);
            case BOOK_MARK -> new OrderSpecifier<>(Order.DESC, product.bookmarkCount);
            default -> new OrderSpecifier<>(Order.DESC, product.id);
        };
    }
}
