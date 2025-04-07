package com.example.projectdemo.domain.board.mapper;

import com.example.projectdemo.domain.board.dto.PostsDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostsMapper {
    /**
     * 새 게시글을 데이터베이스에 저장합니다.
     *
     * @param postsDTO 저장할 게시글 정보
     * @return 영향받은 행 수
     */
    void insertPost(PostsDTO postsDTO);

    // 게시글 목록 조회
    List<PostsDTO> getAllPosts();

    // 게시글 상세 조회
    PostsDTO getPostById(Integer id);


}
