package tn.esprit.article.articleservice.modules.article.entity;

import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "article")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;

    @Column(nullable = false, length = 255)
    private String titre;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleCategory categorie;

    @Column(length = 512)
    private String image;

    @Column(length = 1000)
    private String resume;

    @Column(nullable = false)
    private Boolean genereParIa;

    @Transient
    private long likeCount;

    @Transient
    private long commentCount;

    @Transient
    private boolean likedByCurrentUser;
}
