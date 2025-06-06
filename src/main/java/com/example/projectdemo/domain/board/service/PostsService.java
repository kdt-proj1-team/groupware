package com.example.projectdemo.domain.board.service;

import com.example.projectdemo.domain.board.dto.PostsDTO;
import com.example.projectdemo.domain.board.mapper.CommentsMapper;
import com.example.projectdemo.domain.board.mapper.PostsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostsService {

    @Autowired
    private PostsMapper postsMapper;

    @Autowired
    private AttachmentsService attachmentsService;

    @Autowired
    private CommentsMapper commentsMapper;

    // 게시글 작성
    @Transactional
    public PostsDTO createPost(int empId, PostsDTO postsDTO) {
        // 게시글 작성자 설정
        postsDTO.setEmpId(empId);

        // 현재 시간으로 작성일시 설정
        postsDTO.setCreatedAt(LocalDateTime.now());

        // 조회수 초기화
        postsDTO.setViews(0);

        // 게시글 저장
        postsMapper.insertPost(postsDTO);

        // 저장된 게시글 반환
        return postsDTO;
    }

    // 통합 게시판 - 사용자가 접근 가능한 모든 게시판의 게시글 조회
    public List<PostsDTO> getAccessiblePosts(Integer empId) {
        // 현재 시간
        LocalDateTime now = LocalDateTime.now();

        // 해당 사용자가 접근 가능한 게시글 목록을 가져옴
        List<PostsDTO> posts = postsMapper.getAccessiblePostsByEmpId(empId);

        // 각 게시글에 댓글 수 설정
        for (PostsDTO post : posts) {
            // 해당 게시글의 댓글 수 조회
            int commentCount = commentsMapper.countByPostId(post.getId());
            // 게시글 DTO에 댓글 수 설정
            post.setCommentCount(commentCount);

            // NEW 표시 여부 확인 (24시간 이내 작성된 게시글)
            LocalDateTime createdAt = post.getCreatedAt();
            if (createdAt != null) {
                // 24시간 이내에 작성된 게시글인지 확인
                boolean isNew = createdAt.isAfter(now.minusHours(24));
                // 여기를 setIsNew에서 setNewPost로 변경
                post.setNewPost(isNew);
            }
        }
        return posts;
    }

    // 특정 게시판의 게시글 목록 조회
    public List<PostsDTO> getPostsByBoardId(Integer boardId) {
        // 해당 게시판의 모든 게시글을 조회
        List<PostsDTO> posts = postsMapper.getPostsByBoardId(boardId);

        // 각 게시글에 댓글 수 설정
        for (PostsDTO post : posts) {
            // 해당 게시글의 댓글 수 조회
            int commentCount = commentsMapper.countByPostId(post.getId());
            // 게시글 DTO에 댓글 수 설정
            post.setCommentCount(commentCount);
        }
        return posts;
    }

    // 특정 게시판의 게시글 목록 중 최신순으로 4개만 조회
    public List<PostsDTO> findTop4ByBoardId(Integer boardId) {
        // 해당 게시판의 모든 게시글을 조회
        List<PostsDTO> posts = postsMapper.findTop4ByBoardId(boardId);

        // 각 게시글에 댓글 수 설정
        for (PostsDTO post : posts) {
            // 해당 게시글의 댓글 수 조회
            int commentCount = commentsMapper.countByPostId(post.getId());
            // 게시글 DTO에 댓글 수 설정
            post.setCommentCount(commentCount);
        }
        return posts;
    }

    ;

    // 게시판 ID로 게시글 수 조회
    public int countPostsByBoardId(Integer boardId) {
        return postsMapper.countPostsByBoardId(boardId);
    }

    // 게시글 상세 조회
    public PostsDTO getPostById(int id) {
        PostsDTO post = postsMapper.getPostById(id);
        if (post == null) {
            throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id=" + id);
        }
        return post;
    }

    // 게시글 수정
    public boolean updatePost(PostsDTO post) {
        // 게시글 존재 여부 확인
        PostsDTO existingPost = getPostById(post.getId());
        if (existingPost == null) {
            return false;
        }

        // 수정 작업 수행
        int result = postsMapper.updatePost(post);
        return result > 0;
    }

    // 게시글 수정 권한 확인
    public boolean canModifyPost(int empId, int postId) {
        PostsDTO post = getPostById(postId);
        // 작성자만 수정 가능 또는 관리자 권한 확인
        return post != null && post.getEmpId() == empId;
    }

    // 게시글 삭제
    public boolean deletePost(int postId) {
        // 첨부파일 먼저 삭제
        attachmentsService.deleteAttachmentsByPostId(postId);

        int result = postsMapper.deletePost(postId);
        return result > 0;
    }

    // 내 게시글 조회
    public List<PostsDTO> getMyPosts(Integer empId) {

        return postsMapper.findPostsByEmpId(empId);
    }

    // 게시글 다중 삭제
    public void deletePostsByIds(List<Integer> ids) {
        postsMapper.deletePostsByIds(ids);
    }

    // 조회수 증가 메소드 (단순히 조회수만 증가)
    public void incrementViewCount(int postId) {
        postsMapper.incrementViewCount(postId);
    }

}
