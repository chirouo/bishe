package com.qdx.bishe.service;

import com.qdx.bishe.dto.CreatePaperRequest;
import com.qdx.bishe.dto.PaperListItemDto;
import com.qdx.bishe.dto.TeacherPaperResultDetailDto;
import com.qdx.bishe.dto.TeacherPaperStudentAnswerDetailDto;

import java.util.List;

public interface PaperService {

    List<PaperListItemDto> listPapers(Long courseId);

    Long createPaper(CreatePaperRequest request, Long currentUserId);

    void publishPaper(Long paperId, Long currentUserId);

    TeacherPaperResultDetailDto getPaperResultDetail(Long paperId);

    TeacherPaperStudentAnswerDetailDto getPaperStudentAnswerDetail(Long paperId, Long studentId);
}
