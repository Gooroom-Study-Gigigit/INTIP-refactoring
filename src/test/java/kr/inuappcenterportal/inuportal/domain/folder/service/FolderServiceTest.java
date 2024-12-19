package kr.inuappcenterportal.inuportal.domain.folder.service;

import kr.inuappcenterportal.inuportal.domain.folder.dto.FolderDto;
import kr.inuappcenterportal.inuportal.domain.folder.dto.FolderPostDto;
import kr.inuappcenterportal.inuportal.domain.folder.dto.FolderResponseDto;
import kr.inuappcenterportal.inuportal.domain.folder.model.Folder;
import kr.inuappcenterportal.inuportal.domain.folder.repository.FolderRepository;
import kr.inuappcenterportal.inuportal.domain.folderPost.model.FolderPost;
import kr.inuappcenterportal.inuportal.domain.folderPost.repository.FolderPostRepository;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.repository.MemberRepository;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.scrap.model.Scrap;
import kr.inuappcenterportal.inuportal.domain.scrap.repository.ScrapRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private FolderPostRepository folderPostRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ScrapRepository scrapRepository;

    @InjectMocks
    private FolderService folderService;

    private Folder folder;
    private FolderDto folderDto;
    private Post post;
    private Scrap scrap;
    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member("20231001", "member1", List.of("ROLE_USER"));
        folderDto = new FolderDto("테스트 폴더1");
        folder = Folder.builder().name("테스트 폴더1").member(member).build();
//        folder.setId(1L);
        ReflectionTestUtils.setField(folder, "id", 1L);

        // Post 객체 초기화
        post = Post.builder().title("Test Post").content("Content").build();
        ReflectionTestUtils.setField(post, "id", 1L);

        // Scrap 객체 초기화
        scrap = Scrap.builder().member(member).post(post).build();
        ReflectionTestUtils.setField(scrap, "id", 1L);
    }

    @Test
    @DisplayName("스크랩 폴더를 생성하면 생성된 폴더의 ID를 반환한다")
    void createFolder() {
        //given : createFolder메서드에서 실행되는 folderRepository.save()가 호출되면, 미리 준비된 folder 객체를 반환
        given(folderRepository.save(any(Folder.class))).willReturn(folder);

        //when
        Long folderId = folderService.createFolder(member, folderDto);

        //then
        assertThat(folderId).isEqualTo(1L);
        verify(folderRepository, times(1)).save(any(Folder.class));
    }

    @Test
    @DisplayName("존재하는 스크랩 폴더의 이름을 수정하면 수정된 폴더의 ID를 반환한다")
    void updateFolder() {
        FolderDto updateDto = new FolderDto("폴더명 수정1");

        // given: updateFolder메서드가 실행되면 findById를 통해 id가 존재하는지 1차 체크를 한다
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));

        // when: updateFolder 메서드 호출
        Long updatedFolderId = folderService.updateFolder(1L, updateDto);

        // then: 폴더 ID와 폴더 이름이 올바르게 업데이트되었는지 검증
        assertThat(updatedFolderId).isEqualTo(1L);
        assertThat(folder.getName()).isEqualTo("폴더명 수정1");
        verify(folderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("폴더 수정 시 존재하지 않는 폴더 ID를 입력하면 MyException이 발생한다")
    void updateFolder_FolderNotFound() {
        // given: findById 메서드에서 폴더를 찾지못한 경우 가정
        given(folderRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then: 예외가 발생하는지 검증
        assertThatThrownBy(() -> folderService.updateFolder(1L, folderDto))
                .isInstanceOf(MyException.class) // 던져진 예외가 MyException 타입인지 확인
                .hasMessageContaining(MyErrorCode.FOLDER_NOT_FOUND.getMessage());

        // folderRepository의 findById가 한 번 호출되었는지 검증
        verify(folderRepository, times(1)).findById(anyLong());
    }

   @Test
   @DisplayName("존재하는 폴더 ID로 삭제 요청 시 폴더가 삭제된다")
   void deleteFolder_Success() {
       // given
       given(folderRepository.findById(1L)).willReturn(Optional.of(folder));

       // when: deleteFolder 메서드 호출
       folderService.deleteFolder(1L);

       // then: 폴더가 삭제되었는지 확인
       verify(folderRepository, times(1)).delete(folder);
   }

    @Test
    @DisplayName("존재하지 않는 폴더 ID로 삭제 요청 시 MyException이 발생한다")
    void deleteFolder_FolderNotFound() {
        // given: 폴더 ID로 폴더를 찾을 수 없는 경우
        given(folderRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then: MyException 발생 여부 확인
        assertThatThrownBy(() -> folderService.deleteFolder(1L))
                .isInstanceOf(MyException.class)
                .hasMessageContaining(MyErrorCode.FOLDER_NOT_FOUND.getMessage());

        // then: delete 메서드가 호출되지 않았음을 검증
        verify(folderRepository, never()).delete(any(Folder.class));
    }


    // insertInFolder은 내부 로직이 많아서 테스트 메서드를 분류함
    @Test
    @DisplayName("폴더에 게시물 추가 성공 - 지정된 폴더에 게시물을 정상적으로 추가하고 ID를 반환합니다.")
    void insertInFolder_Success() {
        // given : FolderPostDto는 postId를 리스트로 담아서 가져오고 테스트에서는 1 이 담겨 있다고 가정
        FolderPostDto folderPostDto = new FolderPostDto(List.of(1L));

        // id가 1인 폴더가 존재한다고 가정
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        // 폴더에 연결된 멤버가 있다고 가정
        given(memberRepository.findById(folder.getMember().getId())).willReturn(Optional.of(member));

        // for문 안의 로직 : folderPostDto로 받아온 리스트 id를 통해 동작
        // 게시물(post) ID가 1L인 게시물이 존재한다고 가정.
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        // 멤버와 게시물 간의 스크랩 데이터가 존재한다고 가정
        given(scrapRepository.findByMemberAndPost(member, post)).willReturn(Optional.of(scrap));
        // 폴더에 이미 해당 게시물이 존재하지 않는다고 가정
        given(folderPostRepository.existsByFolderAndPost(folder, post)).willReturn(false);

        // when
        Long folderId = folderService.insertInFolder(1L, folderPostDto);

        // then
        assertThat(folderId).isEqualTo(1L);
        verify(folderPostRepository, times(1)).save(any(FolderPost.class));
    }
    @Test
    @DisplayName("폴더에 게시물 추가 실패 - 존재하지 않는 폴더로 인해 추가할 수 없습니다.")
    void insertInFolder_FolderNotFound() {
        // given
        FolderPostDto folderPostDto = new FolderPostDto(List.of(1L));
        given(folderRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> folderService.insertInFolder(1L, folderPostDto))
                .isInstanceOf(MyException.class)
                .hasMessageContaining(MyErrorCode.FOLDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("폴더에 게시물 추가 실패 - 폴더에 추가하려는 게시물을 찾을 수 없습니다.")
    void insertInFolder_PostNotFound() {
        // given
        FolderPostDto folderPostDto = new FolderPostDto(List.of(1L));
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> folderService.insertInFolder(1L, folderPostDto))
                .isInstanceOf(MyException.class)
                .hasMessageContaining(MyErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("폴더에 게시물 추가 실패 - 이미 폴더에 존재하는 중복된 게시물입니다.")
    void insertInFolder_DuplicatePost() {
        // given
        FolderPostDto folderPostDto = new FolderPostDto(List.of(1L));
        given(folderRepository.findById(1L)).willReturn(Optional.of(folder));
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(scrapRepository.findByMemberAndPost(member, post)).willReturn(Optional.of(scrap));
        given(folderPostRepository.existsByFolderAndPost(folder, post)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> folderService.insertInFolder(1L, folderPostDto))
                .isInstanceOf(MyException.class)
                .hasMessageContaining(MyErrorCode.POST_DUPLICATE_FOLDER.getMessage());
    }
    
}