package cz.dcos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = "actionjbosststxtable")
public class Action {

    @Id
    private String uidstring;
    private Integer statetype;
    private Integer hidden;
    private String typename;
    @JdbcType(BinaryJdbcType.class)
    private byte[] objectstate;
}
