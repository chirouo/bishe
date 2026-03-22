package com.qdx.bishe.dto;

import lombok.Data;

import javax.validation.Valid;
import java.util.List;

@Data
public class SubmitStudentExamRequest {

    @Valid
    private List<SubmitStudentAnswerRequest> answers;
}
