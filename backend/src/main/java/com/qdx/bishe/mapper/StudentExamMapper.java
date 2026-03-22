package com.qdx.bishe.mapper;

import com.qdx.bishe.dto.StudentExamDetailDto;
import com.qdx.bishe.dto.StudentExamListItemDto;
import com.qdx.bishe.dto.StudentExamQuestionDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentExamMapper {

    @Select({
            "SELECT p.id AS paperId, p.title, c.course_name AS courseName,",
            "p.total_score AS paperTotalScore,",
            "COALESCE(er.status, 'NOT_STARTED') AS examStatus,",
            "er.total_score AS studentScore, er.submitted_at AS submittedAt",
            "FROM paper p",
            "LEFT JOIN course c ON p.course_id = c.id",
            "LEFT JOIN exam_record er ON er.paper_id = p.id AND er.student_id = #{studentId}",
            "WHERE p.status = 'PUBLISHED'",
            "ORDER BY p.id DESC"
    })
    List<StudentExamListItemDto> selectStudentExams(@Param("studentId") Long studentId);

    @Select({
            "SELECT p.id AS paperId, p.title, c.course_name AS courseName,",
            "p.total_score AS paperTotalScore,",
            "COALESCE(er.status, 'NOT_STARTED') AS examStatus,",
            "er.total_score AS studentScore, er.auto_score AS autoScore,",
            "er.subjective_score AS subjectiveScore, er.submitted_at AS submittedAt",
            "FROM paper p",
            "LEFT JOIN course c ON p.course_id = c.id",
            "LEFT JOIN exam_record er ON er.paper_id = p.id AND er.student_id = #{studentId}",
            "WHERE p.id = #{paperId} AND p.status = 'PUBLISHED'",
            "LIMIT 1"
    })
    StudentExamDetailDto selectStudentExamDetail(@Param("paperId") Long paperId,
                                                 @Param("studentId") Long studentId);

    @Select({
            "SELECT pq.question_id AS questionId, pq.question_order AS questionOrder,",
            "q.question_type AS questionType, q.stem, pq.score,",
            "sa.answer_content AS answerContent, sa.score AS gainedScore, sa.feedback",
            "FROM paper_question pq",
            "INNER JOIN question q ON pq.question_id = q.id",
            "LEFT JOIN exam_record er ON er.paper_id = pq.paper_id AND er.student_id = #{studentId}",
            "LEFT JOIN student_answer sa ON sa.exam_record_id = er.id AND sa.question_id = pq.question_id",
            "WHERE pq.paper_id = #{paperId}",
            "ORDER BY pq.question_order ASC"
    })
    List<StudentExamQuestionDto> selectStudentExamQuestions(@Param("paperId") Long paperId,
                                                            @Param("studentId") Long studentId);
}
