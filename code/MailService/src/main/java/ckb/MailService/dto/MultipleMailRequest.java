package ckb.MailService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MultipleMailRequest {
    private String userIDs;
    private String subject;
    private String content;
}