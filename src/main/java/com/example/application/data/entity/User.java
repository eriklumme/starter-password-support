package com.example.application.data.entity;

import javax.persistence.Entity;
import javax.persistence.Lob;
import java.time.LocalDate;
import com.example.application.data.AbstractEntity;
import javax.validation.constraints.Email;

@Entity
public class User extends AbstractEntity {

  private String profilePicture;
  @Email
  private String email;
  private Password password;

  @Lob
  public String getProfilePicture() {
    return profilePicture;
  }

  public void setProfilePicture(String profilePicture) {
    this.profilePicture = profilePicture;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Password getPassword() {
    return password;
  }

  public void setPassword(Password password) {
    this.password = password;
  }

}
