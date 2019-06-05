package com.hs.batch.dao.notice;

import com.hs.batch.dto.notice.NoticeSend;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * mapper interface
 *
 * findNoticeSendListByMemberNumber 와 createNoticeSend
 * 두개는 reader, writer 에서 queryId 로 사용하므로
 * 별도 interface 에 선언하지 않아도 상관없음
 */
@Mapper
public interface NoticeMapper {
    List<NoticeSend> findNoticeSendByMemberNumber(String memberNumber);

    int createNoticeSend(NoticeSend noticeSend);
}
