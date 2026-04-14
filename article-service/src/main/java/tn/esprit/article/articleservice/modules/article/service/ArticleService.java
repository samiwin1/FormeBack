package tn.esprit.article.articleservice.modules.article.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.article.articleservice.modules.article.entity.Article;
import tn.esprit.article.articleservice.modules.article.entity.ArticleComment;
import tn.esprit.article.articleservice.modules.article.entity.ArticleLike;
import tn.esprit.article.articleservice.modules.article.repository.ArticleRepository;
import tn.esprit.article.articleservice.modules.article.repository.ArticleCommentRepository;
import tn.esprit.article.articleservice.modules.article.repository.ArticleLikeRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ArticleService implements IArticleService {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "fr", "ar");

    private final ArticleRepository articleRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleCommentRepository articleCommentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${translation.libre.base-url:http://localhost:5000}")
    private String libreTranslateBaseUrl;

    @Value("${translation.libre.api-key:}")
    private String libreTranslateApiKey;

    @Value("${translation.mymemory.enabled:true}")
    private boolean myMemoryFallbackEnabled;

    @Value("${comment.moderation.bad-words:fuck,shit,bitch,asshole,bastard,merde,putain,salope}")
    private String badWordsConfig;

    @Override
    @Transactional(readOnly = true)
    public List<Article> getAllArticles(Long userId) {
        return articleRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getId() == null ? 0 : b.getId(), a.getId() == null ? 0 : a.getId()))
                .map(article -> enrich(article, userId))
                .toList();
    }

    @Override
    @Transactional
    public Article addArticle(Article article) {
        return articleRepository.save(article);
    }

    @Override
    @Transactional(readOnly = true)
    public Article getArticle(Long id, Long userId) {
        return articleRepository.findById(id)
                .map(article -> enrich(article, userId))
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable avec id=" + id));
    }

    @Override
    @Transactional
    public Article updateArticle(Long id, Article article) {
        Article existing = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable avec id=" + id));

        existing.setTitre(article.getTitre());
        existing.setContenu(article.getContenu());
        existing.setCategorie(article.getCategorie());
        existing.setImage(article.getImage());
        existing.setResume(article.getResume());
        existing.setGenereParIa(article.getGenereParIa());
        if (article.getOwnerId() != null) {
            existing.setOwnerId(article.getOwnerId());
        }
        return articleRepository.save(existing);
    }

    @Override
    @Transactional
    public void removeArticle(Long id, Long requesterId, boolean isAdmin) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable avec id=" + id));

        boolean ownerDelete = requesterId != null && requesterId.equals(article.getOwnerId());
        if (!isAdmin && !ownerDelete) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only admin or the user who posted this article can delete it");
        }

        articleLikeRepository.deleteByArticleId(id);
        articleCommentRepository.deleteByArticleId(id);
        articleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Article toggleLike(Long articleId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable avec id=" + articleId));

        ArticleLike existing = articleLikeRepository.findByArticleIdAndUserId(articleId, userId).orElse(null);
        if (existing != null) {
            articleLikeRepository.delete(existing);
        } else {
            articleLikeRepository.save(ArticleLike.builder()
                    .article(article)
                    .userId(userId)
                    .build());
        }

        return enrich(article, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleComment> getComments(Long articleId) {
        return articleCommentRepository.findByArticleIdOrderByIdAsc(articleId).stream()
                .map(comment -> {
                    comment.setContent(maskBadWords(comment.getContent()));
                    return comment;
                })
                .toList();
    }

    @Override
    @Transactional
    public ArticleComment addComment(Long articleId, ArticleComment comment) {
        if (comment.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("content is required");
        }
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable avec id=" + articleId));
        comment.setId(null);
        comment.setArticle(article);
        comment.setContent(maskBadWords(comment.getContent().trim()));
        return articleCommentRepository.save(comment);
    }

    @Override
    @Transactional
    public ArticleComment updateComment(Long articleId, Long commentId, ArticleComment payload, Long requesterId, boolean isAdmin) {
        if (requesterId == null) {
            throw new IllegalArgumentException("requesterId is required");
        }
        if (payload == null || payload.getContent() == null || payload.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("content is required");
        }

        ArticleComment existing = articleCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment introuvable avec id=" + commentId));

        Long commentArticleId = existing.getArticle() == null ? null : existing.getArticle().getId();
        if (commentArticleId == null || !commentArticleId.equals(articleId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment does not belong to this article");
        }

        boolean ownCommentEdit = requesterId.equals(existing.getUserId());
        if (!isAdmin && !ownCommentEdit) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only admin or the user who posted this comment can edit it");
        }

        existing.setContent(maskBadWords(payload.getContent().trim()));
        return articleCommentRepository.save(existing);
    }

    @Override
    @Transactional
    public void removeComment(Long articleId, Long commentId, Long requesterId, boolean isSuperAdmin) {
        if (requesterId == null) {
            throw new IllegalArgumentException("requesterId is required");
        }

        ArticleComment comment = articleCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment introuvable avec id=" + commentId));

        Long commentArticleId = comment.getArticle() == null ? null : comment.getArticle().getId();
        if (commentArticleId == null || !commentArticleId.equals(articleId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment does not belong to this article");
        }

        boolean ownCommentDelete = requesterId.equals(comment.getUserId());
        if (!isSuperAdmin && !ownCommentDelete) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only super admin or the user who posted this comment can delete it");
        }

        articleCommentRepository.deleteById(commentId);
    }

    private String maskBadWords(String content) {
        String maskedContent = content;
        List<String> badWords = Arrays.stream(badWordsConfig.split(","))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .toList();

        for (String badWord : badWords) {
            Pattern pattern = Pattern.compile("(?i)\\b" + Pattern.quote(badWord) + "\\b");
            Matcher matcher = pattern.matcher(maskedContent);
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(buffer, "******");
            }
            matcher.appendTail(buffer);
            maskedContent = buffer.toString();
        }

        return maskedContent;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> translateArticle(Long articleId, String targetLanguage) {
        String lang = targetLanguage == null ? "" : targetLanguage.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_LANGUAGES.contains(lang)) {
            throw new IllegalArgumentException("targetLanguage must be one of: en, fr, ar");
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable avec id=" + articleId));

        String translatedTitle = translateText(article.getTitre(), lang);
        String translatedSummary = article.getResume() == null ? null : translateText(article.getResume(), lang);
        String translatedContent = translateText(article.getContenu(), lang);

        Map<String, Object> response = new HashMap<>();
        response.put("language", lang);
        response.put("titre", translatedTitle);
        response.put("resume", translatedSummary);
        response.put("contenu", translatedContent);
        return response;
    }

    private String translateText(String text, String targetLanguage) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return text;
        }

        String url = libreTranslateBaseUrl.endsWith("/")
                ? libreTranslateBaseUrl + "translate"
                : libreTranslateBaseUrl + "/translate";

        Map<String, Object> payload = new HashMap<>();
        payload.put("q", text);
        payload.put("source", "auto");
        payload.put("target", targetLanguage);
        payload.put("format", "text");
        if (libreTranslateApiKey != null && !libreTranslateApiKey.isBlank()) {
            payload.put("api_key", libreTranslateApiKey);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Map.class);
            Map body = response.getBody();
            if (body == null || body.get("translatedText") == null) {
                throw new IllegalStateException("Translation provider returned an invalid response");
            }
            return String.valueOf(body.get("translatedText"));
        } catch (RestClientException ex) {
            if (myMemoryFallbackEnabled) {
                String fallback = translateWithMyMemory(text, targetLanguage);
                if (fallback != null && !fallback.isBlank()) {
                    return fallback;
                }
            }
            throw new IllegalStateException("Translation service unavailable", ex);
        }
    }

    private String translateWithMyMemory(String text, String targetLanguage) {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        List<String> sourceCandidates = List.of("en", "fr", "ar");

        for (String sourceLang : sourceCandidates) {
            if (sourceLang.equalsIgnoreCase(targetLanguage)) {
                continue;
            }

            String langpair = sourceLang + "|" + targetLanguage;
            String url = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=" + langpair;

            try {
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                Map body = response.getBody();
                if (body == null) {
                    continue;
                }
                Object responseDataObj = body.get("responseData");
                if (responseDataObj instanceof Map responseData) {
                    Object translated = responseData.get("translatedText");
                    if (translated == null) {
                        continue;
                    }
                    String translatedText = String.valueOf(translated).trim();
                    if (translatedText.isEmpty()) {
                        continue;
                    }
                    String upper = translatedText.toUpperCase(Locale.ROOT);
                    if (upper.startsWith("PLEASE SELECT TWO DISTINCT LANGUAGES") || upper.startsWith("INVALID LANGUAGE PAIR")) {
                        continue;
                    }
                    return translatedText;
                }
            } catch (RestClientException ignored) {
                // Try next source candidate.
            }
        }

        return null;
    }

    private Article enrich(Article article, Long userId) {
        long likeCount = articleLikeRepository.countByArticleId(article.getId());
        long commentCount = articleCommentRepository.countByArticleId(article.getId());
        boolean likedByCurrentUser = userId != null && articleLikeRepository.existsByArticleIdAndUserId(article.getId(), userId);

        article.setLikeCount(likeCount);
        article.setCommentCount(commentCount);
        article.setLikedByCurrentUser(likedByCurrentUser);
        return article;
    }
}
