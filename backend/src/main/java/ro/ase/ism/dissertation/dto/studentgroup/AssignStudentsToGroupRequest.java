package ro.ase.ism.dissertation.dto.studentgroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignStudentsToGroupRequest {
    private List<Integer> studentIds;
}
