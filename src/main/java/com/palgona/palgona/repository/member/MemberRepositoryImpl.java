package com.palgona.palgona.repository.member;

import static com.palgona.palgona.domain.member.QMember.member;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.dto.MemberResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private static final int PAGE_SIZE = 20;

    private final JPAQueryFactory queryFactory;

    @Override
    public SliceResponse<MemberResponse> findAllOrderByIdDesc(String cursor) {
        List<MemberResponse> members = queryFactory.select(Projections.constructor(MemberResponse.class,
                        member.id,
                        member.nickName,
                        member.profileImage))
                .from(member)
                .where(ltMemberId(cursor))
                .orderBy(member.id.desc())
                .limit(PAGE_SIZE + 1)
                .fetch();

        return convertToSlice(members);
    }

    private BooleanExpression ltMemberId(String cursor) {
        if (cursor != null) {
            return member.id.lt(Long.valueOf(cursor));
        }

        return null;
    }

    private SliceResponse<MemberResponse> convertToSlice(List<MemberResponse> members) {
        boolean hasNext = existNextPage(members);
        String nextCursor = generateCursor(members);
        return SliceResponse.of(members, hasNext, nextCursor);
    }

    private String generateCursor(List<MemberResponse> members) {
        MemberResponse lastMember = members.get(members.size() - 1);
        return String.valueOf(lastMember.id());
    }

    private boolean existNextPage(List<MemberResponse> members) {
        if (members.size() > PAGE_SIZE) {
            members.remove(PAGE_SIZE);
            return true;
        }

        return false;
    }
}
