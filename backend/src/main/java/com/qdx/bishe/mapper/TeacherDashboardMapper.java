package com.qdx.bishe.mapper;

import com.qdx.bishe.dto.TeacherDashboardOverviewDto;
import org.apache.ibatis.annotations.Select;

public interface TeacherDashboardMapper {

    @Select("SELECT " +
            "(SELECT COUNT(*) FROM course WHERE status = 1) AS courseCount, " +
            "(SELECT COUNT(*) FROM knowledge_point) AS knowledgePointCount, " +
            "(SELECT COUNT(*) FROM question) AS questionCount, " +
            "(SELECT COUNT(*) FROM paper WHERE status = 'PUBLISHED') AS publishedPaperCount")
    TeacherDashboardOverviewDto selectOverview();
}

