package com.sample.operator.app.dto.account;

import com.sample.operator.app.jpa.account.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnabledRoleModel {
    Role role;
    boolean enabled;
}
