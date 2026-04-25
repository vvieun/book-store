package ru.bmstu.iu7.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "publishers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publisher_id")
    private Long publisherId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "website", length = 255)
    private String website;
}
