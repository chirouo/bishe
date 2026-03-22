package com.qdx.bishe.mapper;

import com.qdx.bishe.dto.TeacherKnowledgeMasteryDto;
import com.qdx.bishe.dto.TeacherPaperTrendDto;
import com.qdx.bishe.dto.TeacherStatisticsSummaryDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TeacherStatisticsMapper {

    @Select({
            "SELECT COUNT(*) AS submittedExamCount,",
            "COUNT(DISTINCT er.student_id) AS studentCount,",
            "COALESCE(AVG(CASE WHEN p.total_score = 0 THEN 0 ELSE er.total_score / p.total_score * 100 END), 0) AS averageScoreRate",
            "FROM exam_record er",
            "INNER JOIN paper p ON er.paper_id = p.id",
            "WHERE er.status = 'SUBMITTED'"
    })
    TeacherStatisticsSummaryDto selectSummary();

    @Select({
            "SELECT p.title AS paperTitle,",
            "COALESCE(AVG(CASE WHEN p.total_score = 0 THEN 0 ELSE er.total_score / p.total_score * 100 END), 0) AS averageScoreRate,",
            "COUNT(*) AS submittedCount",
            "FROM exam_record er",
            "INNER JOIN paper p ON er.paper_id = p.id",
            "WHERE er.status = 'SUBMITTED'",
            "GROUP BY p.id, p.title",
            "ORDER BY p.id ASC"
    })
    List<TeacherPaperTrendDto> selectPaperTrends();

    @Select({
            "SELECT q.knowledge_point_id AS knowledgePointId, kp.point_name AS pointName,",
            "COALESCE(AVG(CASE WHEN pq.score = 0 THEN 0 ELSE sa.score / pq.score * 100 END), 0) AS masteryRate",
            "FROM student_answer sa",
            "INNER JOIN exam_record er ON sa.exam_record_id = er.id",
            "INNER JOIN question q ON sa.question_id = q.id",
            "INNER JOIN knowledge_point kp ON q.knowledge_point_id = kp.id",
            "INNER JOIN paper_question pq ON pq.paper_id = er.paper_id AND pq.question_id = sa.question_id",
            "WHERE er.status = 'SUBMITTED'",
            "GROUP BY q.knowledge_point_id, kp.point_name",
            "ORDER BY masteryRate ASC, q.knowledge_point_id ASC"
    })
    List<TeacherKnowledgeMasteryDto> selectKnowledgeMastery();

    @Select({
            "SELECT q.knowledge_point_id AS knowledgePointId, kp.point_name AS pointName,",
            "COALESCE(AVG(CASE WHEN pq.score = 0 THEN 0 ELSE sa.score / pq.score * 100 END), 0) AS masteryRate",
            "FROM student_answer sa",
            "INNER JOIN exam_record er ON sa.exam_record_id = er.id",
            "INNER JOIN question q ON sa.question_id = q.id",
            "INNER JOIN knowledge_point kp ON q.knowledge_point_id = kp.id",
            "INNER JOIN paper_question pq ON pq.paper_id = er.paper_id AND pq.question_id = sa.question_id",
            "WHERE er.status = 'SUBMITTED' AND q.course_id = #{courseId}",
            "GROUP BY q.knowledge_point_id, kp.point_name",
            "ORDER BY masteryRate ASC, q.knowledge_point_id ASC"
    })
    List<TeacherKnowledgeMasteryDto> selectKnowledgeMasteryByCourse(@Param("courseId") Long courseId);
}
