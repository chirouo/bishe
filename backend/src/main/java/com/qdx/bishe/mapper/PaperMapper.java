package com.qdx.bishe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qdx.bishe.dto.PaperListItemDto;
import com.qdx.bishe.dto.TeacherPaperQuestionAnswerDto;
import com.qdx.bishe.dto.TeacherPaperResultDetailDto;
import com.qdx.bishe.dto.TeacherPaperStudentAnswerDetailDto;
import com.qdx.bishe.dto.TeacherPaperStudentResultDto;
import com.qdx.bishe.entity.Paper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PaperMapper extends BaseMapper<Paper> {

    @Select({
            "<script>",
            "SELECT p.id, p.course_id AS courseId, c.course_name AS courseName,",
            "p.title, p.description, p.total_score AS totalScore, p.status,",
            "COUNT(pq.id) AS questionCount",
            "FROM paper p",
            "LEFT JOIN course c ON p.course_id = c.id",
            "LEFT JOIN paper_question pq ON p.id = pq.paper_id",
            "WHERE 1 = 1",
            "<if test='courseId != null'>",
            "AND p.course_id = #{courseId}",
            "</if>",
            "GROUP BY p.id, p.course_id, c.course_name, p.title, p.description, p.total_score, p.status",
            "ORDER BY p.id DESC",
            "</script>"
    })
    List<PaperListItemDto> selectPaperList(@Param("courseId") Long courseId);

    @Select({
            "SELECT p.id AS paperId, p.title, c.course_name AS courseName, p.status, p.total_score AS totalScore,",
            "COUNT(CASE WHEN er.status = 'SUBMITTED' THEN 1 END) AS submittedCount,",
            "COALESCE(AVG(CASE WHEN er.status = 'SUBMITTED' AND p.total_score <> 0 THEN er.total_score / p.total_score * 100 END), 0) AS averageScoreRate",
            "FROM paper p",
            "LEFT JOIN course c ON p.course_id = c.id",
            "LEFT JOIN exam_record er ON er.paper_id = p.id",
            "WHERE p.id = #{paperId}",
            "GROUP BY p.id, p.title, c.course_name, p.status, p.total_score"
    })
    TeacherPaperResultDetailDto selectPaperResultDetail(@Param("paperId") Long paperId);

    @Select({
            "SELECT er.student_id AS studentId, u.real_name AS studentName,",
            "er.total_score AS totalScore, er.auto_score AS autoScore, er.subjective_score AS subjectiveScore,",
            "CASE WHEN p.total_score = 0 THEN 0 ELSE er.total_score / p.total_score * 100 END AS scoreRate,",
            "er.status, er.submitted_at AS submittedAt",
            "FROM exam_record er",
            "INNER JOIN paper p ON er.paper_id = p.id",
            "LEFT JOIN sys_user u ON er.student_id = u.id",
            "WHERE er.paper_id = #{paperId} AND er.status = 'SUBMITTED'",
            "ORDER BY er.total_score DESC, er.submitted_at ASC, er.id ASC"
    })
    List<TeacherPaperStudentResultDto> selectPaperStudentResults(@Param("paperId") Long paperId);

    @Select({
            "SELECT p.id AS paperId, p.title, c.course_name AS courseName, p.total_score AS totalScore,",
            "u.id AS studentId, u.real_name AS studentName, er.status, er.submitted_at AS submittedAt,",
            "er.total_score AS studentTotalScore, er.auto_score AS autoScore, er.subjective_score AS subjectiveScore",
            "FROM exam_record er",
            "INNER JOIN paper p ON er.paper_id = p.id",
            "LEFT JOIN course c ON p.course_id = c.id",
            "LEFT JOIN sys_user u ON er.student_id = u.id",
            "WHERE er.paper_id = #{paperId} AND er.student_id = #{studentId} AND er.status = 'SUBMITTED'",
            "LIMIT 1"
    })
    TeacherPaperStudentAnswerDetailDto selectPaperStudentAnswerDetail(@Param("paperId") Long paperId,
                                                                      @Param("studentId") Long studentId);

    @Select({
            "SELECT q.id AS questionId, pq.question_order AS questionOrder, q.question_type AS questionType,",
            "q.stem, pq.score AS fullScore, q.answer AS correctAnswer, sa.answer_content AS studentAnswer,",
            "sa.is_correct AS isCorrect, sa.score AS gainedScore, sa.feedback",
            "FROM exam_record er",
            "INNER JOIN paper_question pq ON pq.paper_id = er.paper_id",
            "INNER JOIN question q ON q.id = pq.question_id",
            "LEFT JOIN student_answer sa ON sa.exam_record_id = er.id AND sa.question_id = pq.question_id",
            "WHERE er.paper_id = #{paperId} AND er.student_id = #{studentId} AND er.status = 'SUBMITTED'",
            "ORDER BY pq.question_order ASC"
    })
    List<TeacherPaperQuestionAnswerDto> selectPaperStudentAnswerQuestions(@Param("paperId") Long paperId,
                                                                          @Param("studentId") Long studentId);
}
