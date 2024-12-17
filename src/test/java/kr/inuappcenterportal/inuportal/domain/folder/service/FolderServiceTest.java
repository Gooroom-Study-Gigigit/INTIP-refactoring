package kr.inuappcenterportal.inuportal.domain.folder.service;

import kr.inuappcenterportal.inuportal.domain.folder.dto.FolderDto;
import kr.inuappcenterportal.inuportal.domain.folder.model.Folder;
import kr.inuappcenterportal.inuportal.domain.folder.repository.FolderRepository;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {
    @Mock
    private FolderRepository folderRepository;

    @Mock
    private Member member;

    @InjectMocks
    private FolderService folderService;

    private Folder folder;
    private FolderDto folderDto;


    @Transactional
    public Long createFolder(Member member, FolderDto folderDto ){
        return folderRepository.save(Folder.builder().name(folderDto.getName()).member(member).build()).getId();
    }

    @Transactional
    public Long updateFolder(Long folderId, FolderDto folderDto){
        Folder folder = folderRepository.findById(folderId).orElseThrow(()->new MyException(MyErrorCode.FOLDER_NOT_FOUND));
        folder.update(folderDto.getName());
        return folderId;
    }

    @BeforeEach
    void setUp() {
        member = new Member("20231001", "member1", List.of("ROLE_USER"));
        folderDto = new FolderDto("테스트 폴더1");
        folder = Folder.builder().name("테스트 폴더1").member(member).build();
//        folder.setId(1L);
        ReflectionTestUtils.setField(folder, "id", 1L);
    }

    @Test
    @DisplayName("스크랩 폴더 생성 테스트")
    void createFolder() {
        //given : createFolder메서드에서 실행되는 folderRepository.save()가 호출되면, 미리 준비된 folder 객체를 반환
        when(folderRepository.save(any(Folder.class))).thenReturn(folder);

        //when
        Long folderId = folderService.createFolder(member, folderDto);

        //then
        assertThat(folderId).isEqualTo(1L);
        verify(folderRepository, times(1)).save(any(Folder.class));
    }

    @Test
    @DisplayName("스크랩 폴더명 수정 테스트")
    void updateFolder() {
        FolderDto updateDto = new FolderDto("폴더명 수정1");

        // given: updateFolder메서드가 실행되면 findById를 통해 id가 존재하는지 1차 체크를 한다
        when(folderRepository.findById(1L)).thenReturn(Optional.of(folder));

        // when: updateFolder 메서드 호출
        Long updatedFolderId = folderService.updateFolder(1L, updateDto);

        // then: 폴더 ID와 폴더 이름이 올바르게 업데이트되었는지 검증
        assertThat(updatedFolderId).isEqualTo(1L);
        assertThat(folder.getName()).isEqualTo("폴더명 수정1");
        verify(folderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("updateFolder메서드 실행중에 폴더id를 찾을 수 없는 경우 테스트")
    void updateFolder_FolderNotFound() {
        // given: findById 메서드에서 폴더를 찾지못한 경우 가정
        when(folderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then: 예외가 발생하는지 검증
        assertThatThrownBy(() -> folderService.updateFolder(1L, folderDto))
                .isInstanceOf(MyException.class) // 던져진 예외가 MyException 타입인지 확인
                .hasMessageContaining(MyErrorCode.FOLDER_NOT_FOUND.getMessage());

        // folderRepository의 findById가 한 번 호출되었는지 검증
        verify(folderRepository, times(1)).findById(anyLong());
    }
}