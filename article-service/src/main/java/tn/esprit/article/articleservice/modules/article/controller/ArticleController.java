package tn.esprit.article.articleservice.modules.article.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.article.articleservice.modules.article.entity.ArticleComment;
import tn.esprit.article.articleservice.modules.article.entity.Article;
import tn.esprit.article.articleservice.modules.article.service.IArticleService;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/articles")
@AllArgsConstructor
public class ArticleController {

    private final IArticleService articleService;

    @GetMapping
    public List<Article> listArticles(@RequestParam(required = false) Long userId) {
        return articleService.getAllArticles(userId);
    }

    @PostMapping
    public Article createArticle(@RequestBody Article article) {
        return articleService.addArticle(article);
    }

    @GetMapping("/{id}")
    public Article getArticle(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return articleService.getArticle(id, userId);
    }

    @PutMapping("/{id}")
    public Article updateArticle(@PathVariable Long id, @RequestBody Article article) {
        return articleService.updateArticle(id, article);
    }

    @DeleteMapping("/{id}")
    public void deleteArticle(
            @PathVariable Long id,
            @RequestParam Long requesterId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {
        articleService.removeArticle(id, requesterId, isAdmin);
    }

    @PostMapping("/{id}/like")
    public Article toggleLike(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Long userId = payload.get("userId") == null ? null : Long.valueOf(String.valueOf(payload.get("userId")));
        return articleService.toggleLike(id, userId);
    }

    @GetMapping("/{id}/comments")
    public List<ArticleComment> getComments(@PathVariable Long id) {
        return articleService.getComments(id);
    }

    @PostMapping("/{id}/comments")
    public ArticleComment addComment(@PathVariable Long id, @RequestBody ArticleComment comment) {
        return articleService.addComment(id, comment);
    }

    @PutMapping("/{articleId}/comments/{commentId}")
    public ArticleComment updateComment(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @RequestParam Long requesterId,
            @RequestParam(defaultValue = "false") boolean isAdmin,
            @RequestBody ArticleComment comment) {
        return articleService.updateComment(articleId, commentId, comment, requesterId, isAdmin);
    }

    @DeleteMapping("/{articleId}/comments/{commentId}")
    public void deleteComment(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @RequestParam Long requesterId,
            @RequestParam(defaultValue = "false") boolean isSuperAdmin) {
        articleService.removeComment(articleId, commentId, requesterId, isSuperAdmin);
    }

    @PostMapping("/{id}/translate")
    public Map<String, Object> translateArticle(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String targetLanguage = payload.get("targetLanguage") == null ? null : String.valueOf(payload.get("targetLanguage"));
        return articleService.translateArticle(id, targetLanguage);
    }
}
