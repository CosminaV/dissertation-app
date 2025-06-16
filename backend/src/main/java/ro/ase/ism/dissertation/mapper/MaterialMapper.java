package ro.ase.ism.dissertation.mapper;

import org.springframework.stereotype.Component;
import ro.ase.ism.dissertation.dto.material.MaterialResponse;
import ro.ase.ism.dissertation.model.material.Material;
import ro.ase.ism.dissertation.utils.FormatUtils;

@Component
public class MaterialMapper {

    public MaterialResponse mapToMaterialResponse(Material material) {
        MaterialResponse.MaterialResponseBuilder builder = MaterialResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .content(material.getContent())
                .filePath(material.getFilePath())
                .uploadDate(material.getUploadDate())
                .lastUpdatedAt(material.getLastUpdatedAt())
                .uploadedBy(FormatUtils.formatFullName(material.getTeacher().getFirstName(), material.getTeacher().getLastName()));

        if (material.getCourseGroup() != null) {
            builder.courseName(material.getCourseGroup().getCourse().getName());
            builder.targetName(material.getCourseGroup().getStudentGroup().getName());
        } else if (material.getCourseCohort() != null) {
            builder.courseName(material.getCourseCohort().getCourse().getName());
            builder.targetName(material.getCourseCohort().getCohort().getName());
        }

        return builder.build();
    }
}
