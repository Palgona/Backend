package com.palgona.palgona.repository.product;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.SortType;
import com.palgona.palgona.dto.response.ProductPageResponse;

public interface ProductRepositoryCustom {

    SliceResponse<ProductPageResponse> findAllByCategoryAndSearchWord(
            Category category, String searchWord, String cursor, SortType sortType, int pageSize);
}
