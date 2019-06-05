package com.hs.batch.service.notice;

import com.hs.batch.dao.notice.NoticeMapper;
import com.hs.batch.dto.notice.NoticeSend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    NoticeMapper noticeMapper;

    @Override
    public List<NoticeSend> findNoticeSend(String memberNumber) {
        List<NoticeSend> result = noticeMapper.findNoticeSendByMemberNumber(memberNumber);

        result.forEach(item -> {
            item.setPushStatus("1");
        });

        return result;
    }

    @Override
    public int createNoticeSend(NoticeSend noticeSend) {
        return noticeMapper.createNoticeSend(noticeSend);
    }
}
