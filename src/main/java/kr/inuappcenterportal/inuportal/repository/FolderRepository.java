package kr.inuappcenterportal.inuportal.repository;

import kr.inuappcenterportal.inuportal.domain.folder.Folder;
import kr.inuappcenterportal.inuportal.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder,Long> {
    List<Folder> findAllByMember(Member member);
}
