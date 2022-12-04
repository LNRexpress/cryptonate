package com.nightsky.cryptonate.model;

import com.nightsky.cryptonate.annotation.Encrypted;
import java.io.Serializable;
import javax.persistence.Id;

/**
 *
 * @author Chris
 */
public class SimpleEntity implements Serializable {

    @Id
    private Long id;

    @Encrypted(aadFieldNames = { "id" })
    private String emailAddress;

    @Encrypted(aadFieldNames = { "id" })
    private Double price;

    @Encrypted(aadFieldNames = { "id" })
    private Float amount;

    @Encrypted(aadFieldNames = { "id" })
    private Integer streetNumber;

    @Encrypted(aadFieldNames = { "id" })
    private Long followers;

    public SimpleEntity() {  }

    public SimpleEntity(Long id) {
        this.id = id;
    }

    public SimpleEntity(Long id, String emailAddress) {
        this.id = id;
        this.emailAddress = emailAddress;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Integer getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(Integer streetNumber) {
        this.streetNumber = streetNumber;
    }

    public Long getFollowers() {
        return followers;
    }

    public void setFollowers(Long followers) {
        this.followers = followers;
    }

    public static class Builder {

        private final SimpleEntity target;

        public Builder() {
            this.target = new SimpleEntity();
        }

        public Builder withId(Long id) {
            target.setId(id);
            return this;
        }

        public Builder withEmailAddress(String emailAddress) {
            target.setEmailAddress(emailAddress);
            return this;
        }

        public Builder withPrice(Double price) {
            target.setPrice(price);
            return this;
        }

        public Builder withAmount(Float amount) {
            target.setAmount(amount);
            return this;
        }

        public Builder withStreetNumber(Integer streetNumber) {
            target.setStreetNumber(streetNumber);
            return this;
        }

        public Builder withFollowers(Long followers) {
            target.setFollowers(followers);
            return this;
        }

        public SimpleEntity build() {
            return target;
        }
    }

}
