package com.hs.batch.service.notice;

import com.hs.batch.dto.notice.NoticeSend;

import java.util.List;

/**
 * notice 처리 서비스
 */
public interface NoticeService {

    /**
     * notice send 테이블 조회
     *
     * @param memberNumber 회원번호
     * @return noticesend 목록
     */
    List<NoticeSend> findNoticeSend(String memberNumber);
}
