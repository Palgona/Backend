package com.palgona.palgona.repository.member;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.dto.MemberResponse;

public interface MemberRepositoryCustom {

    SliceResponse<MemberResponse> findAllOrderByIdDesc(String cursor);
}
