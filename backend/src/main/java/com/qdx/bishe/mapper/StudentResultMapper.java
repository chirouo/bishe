package com.qdx.bishe.mapper;

import com.qdx.bishe.dto.StudentKnowledgeMasteryDto;
import com.qdx.bishe.dto.StudentResultSummaryDto;
import com.qdx.bishe.dto.StudentScoreTrendDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentResultMapper {

    @Select({
            "SELECT COUNT(*) AS completedExamCount,",
            "COALESCE(AVG(CASE WHEN p.total_score = 0 THEN 0 ELSE er.total_score / p.total_score * 100 END), 0) AS averageScoreRate",
            "FROM exam_record er",
            "INNER JOIN paper p ON er.paper_id = p.id",
            "WHERE er.student_id = #{studentId} AND er.status = 'SUBMITTED'"
    })
    StudentResultSummaryDto selectStudentResultSummary(@Param("studentId") Long studentId);

    @Select({
            "SELECT p.title AS paperTitle, er.total_score AS score,",
            "p.total_score AS paperTotalScore, er.submitted_at AS submittedAt",
            "FROM exam_record er",
            "INNER JOIN paper p ON er.paper_id = p.id",
            "WHERE er.student_id = #{studentId} AND er.status = 'SUBMITTED'",
            "ORDER BY er.submitted_at ASC, er.id ASC"
    })
    List<StudentScoreTrendDto> selectStudentScoreTrends(@Param("studentId") Long studentId);

    @Select({
            "SELECT q.knowledge_point_id AS knowledgePointId, kp.point_name AS pointName,",
            "COALESCE(SUM(sa.score) / NULLIF(SUM(pq.score), 0) * 100, 0) AS masteryRate",
            "FROM student_answer sa",
            "INNER JOIN exam_record er ON sa.exam_record_id = er.id",
            "INNER JOIN question q ON sa.question_id = q.id",
            "INNER JOIN knowledge_point kp ON q.knowledge_point_id = kp.id",
            "INNER JOIN paper_question pq ON pq.paper_id = er.paper_id AND pq.question_id = sa.question_id",
            "WHERE er.student_id = #{studentId} AND er.status = 'SUBMITTED'",
            "GROUP BY q.knowledge_point_id, kp.point_name",
            "ORDER BY masteryRate ASC, q.knowledge_point_id ASC"
    })
    List<StudentKnowledgeMasteryDto> selectStudentKnowledgeMastery(@Param("studentId") Long studentId);
}
