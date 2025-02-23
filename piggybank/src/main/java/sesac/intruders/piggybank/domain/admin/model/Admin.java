package sesac.intruders.piggybank.domain.admin.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sesac.intruders.piggybank.global.converter.EncryptConverter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
@Getter
@Setter
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String adminId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 255)
    @Convert(converter = EncryptConverter.class)
    private String name;

    @Column(nullable = false, length = 255)
    @Convert(converter = EncryptConverter.class)
    private String email;

    @Column(nullable = false, length = 255)
    @Convert(converter = EncryptConverter.class)
    private String phoneNumber;

    @Column
    private LocalDateTime lastLogin;

    // Throws an exception if the admin is not found
    public Admin orElseThrow(Object invalidAdminCredentials) {
        throw new RuntimeException(invalidAdminCredentials.toString());
    }

    // Getter for password
    public String getPassword() {
        return password;
    }

    // Getter for adminId
    public String getAdminId() {
        return adminId;
    }
}
