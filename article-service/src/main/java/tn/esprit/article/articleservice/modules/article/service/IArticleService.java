package tn.esprit.article.articleservice.modules.article.service;

import tn.esprit.article.articleservice.modules.article.entity.ArticleComment;
import tn.esprit.article.articleservice.modules.article.entity.Article;

import java.util.List;
import java.util.Map;

public interface IArticleService {
    List<Article> getAllArticles(Long userId);
    Article addArticle(Article article);
    Article getArticle(Long id, Long userId);
    Article updateArticle(Long id, Article article);
    void removeArticle(Long id, Long requesterId, boolean isAdmin);
    Article toggleLike(Long articleId, Long userId);
    List<ArticleComment> getComments(Long articleId);
    ArticleComment addComment(Long articleId, ArticleComment comment);
    ArticleComment updateComment(Long articleId, Long commentId, ArticleComment comment, Long requesterId, boolean isAdmin);
    void removeComment(Long articleId, Long commentId, Long requesterId, boolean isSuperAdmin);
    Map<String, Object> translateArticle(Long articleId, String targetLanguage);
}
