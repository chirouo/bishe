package com.qdx.bishe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qdx.bishe.dto.KnowledgePointDto;
import com.qdx.bishe.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface KnowledgePointMapper extends BaseMapper<KnowledgePoint> {

    @Select({
            "<script>",
            "SELECT kp.id, kp.course_id AS courseId, c.course_name AS courseName,",
            "kp.point_name AS pointName, kp.parent_id AS parentId, kp.difficulty, kp.description",
            "FROM knowledge_point kp",
            "LEFT JOIN course c ON kp.course_id = c.id",
            "WHERE 1 = 1",
            "<if test='courseId != null'>",
            "AND kp.course_id = #{courseId}",
            "</if>",
            "ORDER BY kp.course_id ASC, kp.id ASC",
            "</script>"
    })
    List<KnowledgePointDto> selectKnowledgePointDtos(@Param("courseId") Long courseId);
}

