package tn.esprit.article.articleservice.modules.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.article.articleservice.modules.article.entity.ArticleLike;

import java.util.Optional;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    long countByArticleId(Long articleId);
    boolean existsByArticleIdAndUserId(Long articleId, Long userId);
    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
    void deleteByArticleIdAndUserId(Long articleId, Long userId);
    void deleteByArticleId(Long articleId);
}