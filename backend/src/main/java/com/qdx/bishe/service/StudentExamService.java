package com.qdx.bishe.service;

import com.qdx.bishe.dto.StudentExamDetailDto;
import com.qdx.bishe.dto.StudentExamListItemDto;
import com.qdx.bishe.dto.StudentExamSubmitResultDto;
import com.qdx.bishe.dto.SubmitStudentExamRequest;

import java.util.List;

public interface StudentExamService {

    List<StudentExamListItemDto> listStudentExams(Long studentId);

    StudentExamDetailDto getStudentExamDetail(Long paperId, Long studentId);

    StudentExamSubmitResultDto submitStudentExam(Long paperId, Long studentId, SubmitStudentExamRequest request);
}
