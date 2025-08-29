package com.sample.operator.app.jpa.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Embeddable
public class MemberInfo {


    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin")
    String memberName;


    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin")
    String employeeNo;
}
