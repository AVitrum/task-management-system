package com.vitrum.api.data.response;

import com.vitrum.api.data.enums.RoleInTeam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {

    private Long id;
    private String name;
    private RoleInTeam role;
    private UserProfileResponse user;
}
