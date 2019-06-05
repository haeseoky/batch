package com.hs.batch.dto.notice;

import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 테스트 notice send dto
 *
 * @Data 는 @Getter @Setter @RequiredArgsConstructor @ToString @EqualsAndHashCode 와 동일
 * mybatis 설정 alias 지정을 어노테이션 @Alias 로 설정
 */
@Data
@Alias("noticeSend")
public class NoticeSend {
    private int noticeId;
    private String memberNumber;
    private String pushStatus;
}