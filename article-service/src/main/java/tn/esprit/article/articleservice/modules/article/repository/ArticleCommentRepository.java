package tn.esprit.article.articleservice.modules.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.article.articleservice.modules.article.entity.ArticleComment;

import java.util.List;

public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
    List<ArticleComment> findByArticleIdOrderByIdAsc(Long articleId);
    long countByArticleId(Long articleId);
    void deleteByArticleId(Long articleId);
}