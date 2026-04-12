package tn.esprit.article.articleservice.modules.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.article.articleservice.modules.article.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
