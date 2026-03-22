package com.qdx.bishe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qdx.bishe.dto.QuestionListItemDto;
import com.qdx.bishe.entity.Question;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QuestionMapper extends BaseMapper<Question> {

    @Select({
            "<script>",
            "SELECT q.id, q.course_id AS courseId, c.course_name AS courseName,",
            "q.knowledge_point_id AS knowledgePointId, kp.point_name AS knowledgePointName,",
            "q.question_type AS questionType, q.stem, q.difficulty, q.answer, q.analysis, q.source",
            "FROM question q",
            "LEFT JOIN course c ON q.course_id = c.id",
            "LEFT JOIN knowledge_point kp ON q.knowledge_point_id = kp.id",
            "WHERE 1 = 1",
            "<if test='courseId != null'>",
            "AND q.course_id = #{courseId}",
            "</if>",
            "<if test='knowledgePointId != null'>",
            "AND q.knowledge_point_id = #{knowledgePointId}",
            "</if>",
            "<if test='questionType != null and questionType != \"\"'>",
            "AND q.question_type = #{questionType}",
            "</if>",
            "ORDER BY q.id DESC",
            "</script>"
    })
    List<QuestionListItemDto> selectQuestionList(@Param("courseId") Long courseId,
                                                 @Param("knowledgePointId") Long knowledgePointId,
                                                 @Param("questionType") String questionType);
}

