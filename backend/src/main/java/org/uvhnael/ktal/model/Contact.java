package org.uvhnael.ktal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contact {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private Long serviceId;
    private String message;
    private String status;
    private String note;
    private String handledBy;
    private String handledAt;
    private String createdAt;
}
