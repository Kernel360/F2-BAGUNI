package baguni.api.service.folder.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import baguni.infra.infrastructure.folder.dto.FolderCommand;
import baguni.infra.infrastructure.folder.dto.FolderMapper;
import baguni.infra.exception.folder.ApiFolderErrorCode;
import baguni.infra.exception.folder.ApiFolderException;
import baguni.api.fixture.FolderFixture;
import baguni.api.fixture.UserFixture;
import baguni.infra.infrastructure.folder.FolderDataHandler;
import baguni.infra.infrastructure.pick.PickDataHandler;
import baguni.infra.infrastructure.sharedFolder.SharedFolderDataHandler;
import baguni.infra.model.folder.Folder;
import baguni.infra.model.folder.FolderType;
import baguni.infra.model.user.User;

@DisplayName("폴더 서비스 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
class FolderServiceUnitTest {

	@Mock
	private FolderDataHandler folderDataHandler;

	@Mock
	private FolderMapper folderMapper;

	@Mock
	private PickDataHandler pickDataHandler;

	@Mock
	private SharedFolderDataHandler sharedFolderDataHandler;

	@InjectMocks
	private FolderService folderService;

	@Test
	@DisplayName("폴더 저장 시 부모폴더가 본인의 폴더면 저장한다.")
	void saveFolder_Should_SaveNewFolder_When_UserIsParentFolderOwner() {
		// given
		Long userId = 1L;
		Long parentFolderId = 2L;
		FolderCommand.Create command = new FolderCommand.Create(userId, "folderName", parentFolderId);
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder parentFolder = FolderFixture
			.builder().id(parentFolderId).user(user).build().get();

		given(folderDataHandler.getFolder(any())).willReturn(parentFolder);
		given(folderDataHandler.saveFolder(any())).willReturn(FolderFixture
			.builder().build().get());

		// when
		folderService.saveFolder(command);

		// then
		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(times(1)).saveFolder(any());
		then(folderMapper).should(times(1)).toResult(any());
	}

	@Test
	@DisplayName("폴더 저장 시 부모폴더가 본인의 폴더가 아니면 예외를 발생시킨다.")
	void saveFolder_Should_ThrowException_When_UserIsNotParentFolderOwner() {
		// given
		Long userId = 1L;
		Long parentFolderOwnerId = 2L;
		Long parentFolderId = 2L;
		FolderCommand.Create command = new FolderCommand.Create(userId, "folderName", parentFolderId);
		User parentFolderOwner = UserFixture
			.builder().id(parentFolderOwnerId).build().get();
		Folder parentFolder = FolderFixture
			.builder().id(parentFolderId).user(parentFolderOwner).build().get();

		given(folderDataHandler.getFolder(any())).willReturn(parentFolder);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.saveFolder(command));
		assertEquals(ApiFolderErrorCode.FOLDER_ACCESS_DENIED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(never()).saveFolder(any());
		then(folderMapper).should(never()).toResult(any());
	}

	@Test
	@DisplayName("변경하려는 폴더의 소유자이고 변경하려는 폴더가 기본폴더가 아니면 변경한다.")
	void updateFolder_Should_updateFolder_When_UserIsOwnerAndFolderIsNotBasicFolder() {
		// given
		Long userId = 1L;
		Long folderId = 2L;
		FolderCommand.Update command = new FolderCommand.Update(userId, folderId, "folderName");
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder folder = FolderFixture
			.builder().id(folderId).user(user).folderType(FolderType.GENERAL).build().get();

		given(folderDataHandler.getFolder(any())).willReturn(folder);

		// when
		folderService.updateFolder(command);

		// then
		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(times(1)).updateFolder(any());
		then(folderMapper).should(times(1)).toResult(any());

	}

	@Test
	@DisplayName("변경하려는 폴더의 소유자가 아니면 예외를 발생시킨다.")
	void updateFolder_Should_ThrowException_When_UserIsNotParentFolderOwner() {
		// given
		Long userId = 1L;
		Long folderOwnerId = 2L;
		Long folderId = 3L;
		FolderCommand.Update command = new FolderCommand.Update(userId, folderId, "folderName");
		User folderOwner = UserFixture
			.builder().id(folderOwnerId).build().get();
		Folder folder = FolderFixture
			.builder().id(folderId).user(folderOwner).build().get();

		given(folderDataHandler.getFolder(any())).willReturn(folder);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.updateFolder(command));
		assertEquals(ApiFolderErrorCode.FOLDER_ACCESS_DENIED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(never()).updateFolder(any());
		then(folderMapper).should(never()).toResult(any());
	}

	@Test
	@DisplayName("변경하려는 폴더가 기본 폴더이면 예외를 발생시킨다.")
	void updateFolder_Should_ThrowException_When_UserIsBasicFolder() {
		// given
		Long userId = 1L;
		Long folderId = 3L;
		FolderCommand.Update command = new FolderCommand.Update(userId, folderId, "folderName");
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder folder = FolderFixture
			.builder().id(folderId).user(user).folderType(FolderType.ROOT).build().get();

		given(folderDataHandler.getFolder(any())).willReturn(folder);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.updateFolder(command));
		assertEquals(ApiFolderErrorCode.BASIC_FOLDER_CANNOT_CHANGED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(never()).updateFolder(any());
		then(folderMapper).should(never()).toResult(any());
	}

	@Test
	@DisplayName("목적지 폴더의 소유자가 아니면 예외를 발생시킨다.")
	void moveFolder_Should_ThrowException_When_UserIsNotDestinationFolderOwner() {
		// given
		Long userId = 1L;
		Long destinationFolderOwnerId = 2L;
		Long destinationFolderId = 3L;
		FolderCommand.Move command = new FolderCommand.Move(
			userId,
			List.of(1L, 2L),
			4L,
			destinationFolderId,
			0
		);
		User destinationFolderOwner = UserFixture
			.builder().id(destinationFolderOwnerId).build().get();
		Folder destinationFolder = FolderFixture
			.builder()
			.id(destinationFolderId)
			.user(destinationFolderOwner)
			.build()
			.get();

		given(folderDataHandler.getFolder(any())).willReturn(destinationFolder);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.moveFolder(command));
		assertEquals(ApiFolderErrorCode.FOLDER_ACCESS_DENIED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(never()).getFolderList(any());
		then(folderDataHandler).should(never()).moveFolderWithinParent(any());
		then(folderDataHandler).should(never()).moveFolderToDifferentParent(any());
	}

	@Test
	@DisplayName("목적지 폴더가 미분류 폴더면 예외를 발생시킨다.")
	void moveFolder_Should_ThrowException_When_DestinationFolderIsUnclassified() {
		// given
		Long userId = 1L;
		Long destinationFolderId = 3L;
		FolderCommand.Move command = new FolderCommand.Move(
			userId,
			List.of(1L, 2L),
			4L,
			destinationFolderId,
			0
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder destinationFolder = FolderFixture
			.builder()
			.id(destinationFolderId)
			.user(user)
			.folderType(FolderType.UNCLASSIFIED)
			.build()
			.get();

		given(folderDataHandler.getFolder(any())).willReturn(destinationFolder);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.moveFolder(command));
		assertEquals(ApiFolderErrorCode.INVALID_TARGET, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(never()).getFolderList(any());
		then(folderDataHandler).should(never()).moveFolderWithinParent(any());
		then(folderDataHandler).should(never()).moveFolderToDifferentParent(any());
	}

	@Test
	@DisplayName("목적지 폴더가 휴지통이면 예외를 발생시킨다.")
	void moveFolder_Should_ThrowException_When_DestinationFolderIsRecycleBin() {
		// given
		Long userId = 1L;
		Long destinationFolderId = 3L;
		FolderCommand.Move command = new FolderCommand.Move(
			userId,
			List.of(1L, 2L),
			4L,
			destinationFolderId,
			0
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder destinationFolder = FolderFixture
			.builder()
			.id(destinationFolderId)
			.user(user)
			.folderType(FolderType.RECYCLE_BIN)
			.build()
			.get();

		given(folderDataHandler.getFolder(any())).willReturn(destinationFolder);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.moveFolder(command));
		assertEquals(ApiFolderErrorCode.INVALID_TARGET, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(never()).getFolderList(any());
		then(folderDataHandler).should(never()).moveFolderWithinParent(any());
		then(folderDataHandler).should(never()).moveFolderToDifferentParent(any());
	}

	@Test
	@DisplayName("이동하려는 폴더들의 소유자가 아니면 예외를 발생시킨다.")
	void moveFolder_Should_ThrowException_When_UserIsNotTargetFolderOwner() {
		// given
		Long userId = 1L;
		Long folderOwnerId = 2L;
		Long destinationFolderId = 1L;
		List<Long> folderIdList = List.of(1L, 2L);
		FolderCommand.Move command = new FolderCommand.Move(
			userId,
			folderIdList,
			4L,
			destinationFolderId,
			0
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		User folderOwner = UserFixture
			.builder().id(folderOwnerId).build().get();
		Folder destinationFolder = FolderFixture
			.builder()
			.id(destinationFolderId)
			.user(user)
			.build()
			.get();
		List<Folder> folderList = folderIdList.stream()
											  .map(id -> FolderFixture
												  .builder().id(id).user(folderOwner).build().get())
											  .toList();
		given(folderDataHandler.getFolder(any())).willReturn(destinationFolder);
		given(folderDataHandler.getFolderList(any())).willReturn(folderList);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.moveFolder(command));
		assertEquals(ApiFolderErrorCode.FOLDER_ACCESS_DENIED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(folderDataHandler).should(never()).moveFolderWithinParent(any());
		then(folderDataHandler).should(never()).moveFolderToDifferentParent(any());
	}

	@Test
	@DisplayName("이동하려는 폴더가 루트폴더면 예외를 발생시킨다.")
	void moveFolder_Should_ThrowException_When_TargetFolderIsRoot() {
		// given
		Long userId = 1L;
		Long destinationFolderId = 1L;
		List<Long> folderIdList = List.of(1L, 2L);
		FolderCommand.Move command = new FolderCommand.Move(
			userId,
			folderIdList,
			4L,
			destinationFolderId,
			0
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder destinationFolder = FolderFixture
			.builder()
			.id(destinationFolderId)
			.user(user)
			.build()
			.get();
		List<Folder> folderList = folderIdList.stream()
											  .map(id -> FolderFixture
												  .builder()
												  .id(id)
												  .user(user)
												  .folderType(FolderType.ROOT)
												  .build()
												  .get())
											  .toList();
		given(folderDataHandler.getFolder(any())).willReturn(destinationFolder);
		given(folderDataHandler.getFolderList(any())).willReturn(folderList);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.moveFolder(command));
		assertEquals(ApiFolderErrorCode.BASIC_FOLDER_CANNOT_CHANGED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(folderDataHandler).should(never()).moveFolderWithinParent(any());
		then(folderDataHandler).should(never()).moveFolderToDifferentParent(any());
	}

	@Test
	@DisplayName("이동하려는 폴더가 미분류폴더면 예외를 발생시킨다.")
	void moveFolder_Should_ThrowException_When_TargetFolderIsUnclassified() {
		// given
		Long userId = 1L;
		Long destinationFolderId = 1L;
		List<Long> folderIdList = List.of(1L, 2L);
		FolderCommand.Move command = new FolderCommand.Move(
			userId,
			folderIdList,
			4L,
			destinationFolderId,
			0
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder destinationFolder = FolderFixture
			.builder()
			.id(destinationFolderId)
			.user(user)
			.build()
			.get();
		List<Folder> folderList = folderIdList.stream()
											  .map(id -> FolderFixture
												  .builder()
												  .id(id)
												  .user(user)
												  .folderType(FolderType.UNCLASSIFIED)
												  .build()
												  .get())
											  .toList();
		given(folderDataHandler.getFolder(any())).willReturn(destinationFolder);
		given(folderDataHandler.getFolderList(any())).willReturn(folderList);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.moveFolder(command));
		assertEquals(ApiFolderErrorCode.BASIC_FOLDER_CANNOT_CHANGED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(folderDataHandler).should(never()).moveFolderWithinParent(any());
		then(folderDataHandler).should(never()).moveFolderToDifferentParent(any());
	}

	@Test
	@DisplayName("이동하려는 폴더가 휴지통면 예외를 발생시킨다.")
	void moveFolder_Should_ThrowException_When_TargetFolderIsRecycleBin() {
		// given
		Long userId = 1L;
		Long destinationFolderId = 1L;
		List<Long> folderIdList = List.of(1L, 2L);
		FolderCommand.Move command = new FolderCommand.Move(
			userId,
			folderIdList,
			4L,
			destinationFolderId,
			0
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder destinationFolder = FolderFixture
			.builder()
			.id(destinationFolderId)
			.user(user)
			.build()
			.get();
		List<Folder> folderList = folderIdList.stream()
											  .map(id -> FolderFixture
												  .builder()
												  .id(id)
												  .user(user)
												  .folderType(FolderType.RECYCLE_BIN)
												  .build()
												  .get())
											  .toList();
		given(folderDataHandler.getFolder(any())).willReturn(destinationFolder);
		given(folderDataHandler.getFolderList(any())).willReturn(folderList);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.moveFolder(command));
		assertEquals(ApiFolderErrorCode.BASIC_FOLDER_CANNOT_CHANGED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(folderDataHandler).should(never()).moveFolderWithinParent(any());
		then(folderDataHandler).should(never()).moveFolderToDifferentParent(any());
	}

	@Test
	@DisplayName("이동하려는 폴더들의 부모폴더가 다르면 예외를 발생시킨다.")
	void moveFolder_Should_ThrowException_When_ParentIsDifferent() {
		// given
		Long userId = 1L;
		Long parentFolderId = 5L;
		Long realParentFolderId = 11L;
		Long destinationFolderId = 3L;
		List<Long> folderIdList = List.of(1L, 2L);
		FolderCommand.Move command = new FolderCommand.Move(
			userId,
			folderIdList,
			parentFolderId,
			destinationFolderId,
			0
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder parentFolder = FolderFixture
			.builder().id(realParentFolderId).build().get();
		Folder destinationFolder = FolderFixture
			.builder().id(destinationFolderId).user(user).build().get();
		List<Folder> folderList = folderIdList.stream()
											  .map(id -> FolderFixture
												  .builder()
												  .id(id)
												  .user(user)
												  .parentFolder(parentFolder)
												  .folderType(FolderType.GENERAL)
												  .build()
												  .get())
											  .toList();

		given(folderDataHandler.getFolder(any())).willReturn(destinationFolder);
		given(folderDataHandler.getFolderList(any())).willReturn(folderList);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.moveFolder(command));
		assertEquals(ApiFolderErrorCode.INVALID_PARENT_FOLDER, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(folderDataHandler).should(never()).moveFolderWithinParent(any());
		then(folderDataHandler).should(never()).moveFolderToDifferentParent(any());
	}

	@Test
	@DisplayName("같은 폴더 내에서 이동이면 moveFolderWithinParent 메소드를 호출한다.")
	void moveFolder_Should_CallMoveFolderWithinParent_When_MoveWithinSameFolder() {
		// given
		Long userId = 1L;
		Long parentFolderId = 5L;
		List<Long> folderIdList = List.of(1L, 2L);
		FolderCommand.Move command = new FolderCommand.Move(
			userId,
			folderIdList,
			parentFolderId,
			parentFolderId,
			0
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder parentFolder = FolderFixture
			.builder().id(parentFolderId).build().get();
		Folder destinationFolder = FolderFixture
			.builder().id(parentFolderId).user(user).build().get();
		List<Folder> folderList = folderIdList.stream()
											  .map(id -> FolderFixture
												  .builder()
												  .id(id)
												  .user(user)
												  .parentFolder(parentFolder)
												  .folderType(FolderType.GENERAL)
												  .build()
												  .get())
											  .toList();

		given(folderDataHandler.getFolder(any())).willReturn(destinationFolder);
		given(folderDataHandler.getFolderList(any())).willReturn(folderList);

		// when
		folderService.moveFolder(command);

		// then
		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(folderDataHandler).should(times(1)).moveFolderWithinParent(any());
		then(folderDataHandler).should(never()).moveFolderToDifferentParent(any());
	}

	@Test
	@DisplayName("다른 폴더로 이동하면 moveFolderToDifferentParent 메소드를 호출한다.")
	void moveFolder_Should_CallMoveFolderToDifferentParent_When_MoveToDifferentFolder() {
		// given
		Long userId = 1L;
		Long parentFolderId = 5L;
		Long destinationFolderId = 3L;
		List<Long> folderIdList = List.of(1L, 2L);
		FolderCommand.Move command = new FolderCommand.Move(
			userId,
			folderIdList,
			parentFolderId,
			destinationFolderId,
			0
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder parentFolder = FolderFixture
			.builder().id(parentFolderId).build().get();
		Folder destinationFolder = FolderFixture
			.builder().id(destinationFolderId).user(user).build().get();
		List<Folder> folderList = folderIdList.stream()
											  .map(id -> FolderFixture
												  .builder()
												  .id(id)
												  .user(user)
												  .parentFolder(parentFolder)
												  .folderType(FolderType.GENERAL)
												  .build()
												  .get())
											  .toList();

		given(folderDataHandler.getFolder(any())).willReturn(destinationFolder);
		given(folderDataHandler.getFolderList(any())).willReturn(folderList);

		// when
		folderService.moveFolder(command);

		// then
		then(folderDataHandler).should(times(1)).getFolder(anyLong());
		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(folderDataHandler).should(never()).moveFolderWithinParent(any());
		then(folderDataHandler).should(times(1)).moveFolderToDifferentParent(any());
	}

	@Test
	@DisplayName("삭제하려는 폴더의 소유자가 아니라면 예외를 발생시킨다.")
	void deleteFolder_Should_ThrowException_When_UserIsNotOwner() {
		// given
		Long userId = 1L;
		Long folderOwnerId = 2L;
		List<Long> folderIdList = List.of(1L, 2L);
		FolderCommand.Delete command = new FolderCommand.Delete(
			userId,
			folderIdList
		);
		User folderOwner = UserFixture
			.builder().id(folderOwnerId).build().get();
		List<Folder> folderList = folderIdList.stream()
											  .map(id -> FolderFixture
												  .builder().id(id).user(folderOwner).build().get())
											  .toList();
		given(folderDataHandler.getFolderList(any())).willReturn(folderList);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.deleteFolder(command));
		assertEquals(ApiFolderErrorCode.FOLDER_ACCESS_DENIED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(sharedFolderDataHandler).should(atLeast(1)).deleteBySourceFolderId(anyLong());
		then(folderDataHandler).should(never()).getFolderListByUserId(any());
		then(pickDataHandler).should(never()).movePickListToRecycleBin(anyLong(), any());
		then(folderDataHandler).should(never()).deleteFolderList(any());
	}

	@Test
	@DisplayName("삭제하려는 폴더가 루트폴더면 예외를 발생시킨다.")
	void deleteFolder_Should_ThrowException_When_TargetFolderIsRoot() {
		// given
		Long userId = 1L;
		List<Long> folderIdList = List.of(1L, 2L);
		FolderCommand.Delete command = new FolderCommand.Delete(
			userId,
			folderIdList
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		List<Folder> folderList = folderIdList.stream()
											  .map(id -> FolderFixture
												  .builder()
												  .id(id)
												  .user(user)
												  .folderType(FolderType.ROOT)
												  .build()
												  .get())
											  .toList();
		given(folderDataHandler.getFolderList(any())).willReturn(folderList);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.deleteFolder(command));
		assertEquals(ApiFolderErrorCode.BASIC_FOLDER_CANNOT_CHANGED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(folderDataHandler).should(never()).getFolderListByUserId(any());
		then(pickDataHandler).should(never()).movePickListToRecycleBin(anyLong(), any());
		then(folderDataHandler).should(never()).deleteFolderList(any());
	}

	@Test
	@DisplayName("삭제하려는 폴더가 미분류폴더면 예외를 발생시킨다.")
	void deleteFolder_Should_ThrowException_When_TargetFolderIsUnclassified() {
		// given
		Long userId = 1L;
		List<Long> folderIdList = List.of(1L, 2L);
		FolderCommand.Delete command = new FolderCommand.Delete(
			userId,
			folderIdList
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		List<Folder> folderList = folderIdList.stream()
											  .map(id -> FolderFixture
												  .builder()
												  .id(id)
												  .user(user)
												  .folderType(FolderType.UNCLASSIFIED)
												  .build()
												  .get())
											  .toList();
		given(folderDataHandler.getFolderList(any())).willReturn(folderList);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.deleteFolder(command));
		assertEquals(ApiFolderErrorCode.BASIC_FOLDER_CANNOT_CHANGED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(sharedFolderDataHandler).should(atLeast(1)).deleteBySourceFolderId(anyLong());
		then(folderDataHandler).should(never()).getFolderListByUserId(any());
		then(pickDataHandler).should(never()).movePickListToRecycleBin(anyLong(), any());
		then(folderDataHandler).should(never()).deleteFolderList(any());
	}

	@Test
	@DisplayName("삭제하려는 폴더가 휴지통이면 예외를 발생시킨다.")
	void deleteFolder_Should_ThrowException_When_TargetFolderIsRecycleBin() {
		// given
		Long userId = 1L;
		List<Long> folderIdList = List.of(1L, 2L);
		FolderCommand.Delete command = new FolderCommand.Delete(
			userId,
			folderIdList
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		List<Folder> folderList = folderIdList.stream()
											  .map(id -> FolderFixture
												  .builder()
												  .id(id)
												  .user(user)
												  .folderType(FolderType.RECYCLE_BIN)
												  .build()
												  .get())
											  .toList();
		given(folderDataHandler.getFolderList(any())).willReturn(folderList);

		// when & then
		var exception = assertThrows(ApiFolderException.class, () -> folderService.deleteFolder(command));
		assertEquals(ApiFolderErrorCode.BASIC_FOLDER_CANNOT_CHANGED, exception.getApiErrorCode());

		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(sharedFolderDataHandler).should(atLeast(1)).deleteBySourceFolderId(anyLong());
		then(folderDataHandler).should(never()).getFolderListByUserId(any());
		then(pickDataHandler).should(never()).movePickListToRecycleBin(anyLong(), any());
		then(folderDataHandler).should(never()).deleteFolderList(any());
	}

	@Test
	@DisplayName("폴더 삭제 시 모든 하위 폴더들을 삭제하고 픽들은 휴지통으로 이동시킨다.")
	void deleteFolder_Should_DeleteAllChildFoldersAndMoveChildPicksToRecycleBin_When_FolderIsDeleted() {
		// given
		Long userId = 1L;
		List<Long> folderIdList = List.of(1L);
		FolderCommand.Delete command = new FolderCommand.Delete(
			userId,
			folderIdList
		);
		User user = UserFixture
			.builder().id(userId).build().get();
		/*
		 * 현재 폴더 3개, 픽6개인 상황
		 * 부모자식 관계는 다음과 같음
		 * 폴더 1 - 폴더3, 픽1, 픽2
		 * 폴더 2 - 픽3, 픽4
		 * 폴더 3 - 픽5, 픽6
		 * 현재 1번폴더를 삭제 했으므로, 픽 1,2,5,6이 삭제되어야함
		 * */
		Folder folder1 = FolderFixture
			.builder()
			.id(1L)
			.user(user)
			.folderType(FolderType.GENERAL)
			.childFolderIdOrderedList(new ArrayList<>(List.of(3L)))
			.childPickIdOrderedList(new ArrayList<>(List.of(1L, 2L)))
			.build()
			.get();
		Folder folder2 = FolderFixture
			.builder()
			.id(2L)
			.user(user)
			.folderType(FolderType.GENERAL)
			.parentFolder(folder1)
			.childPickIdOrderedList(new ArrayList<>(List.of(3L, 4L)))
			.build()
			.get();
		Folder folder3 = FolderFixture
			.builder()
			.id(3L)
			.user(user)
			.folderType(FolderType.GENERAL)
			.childPickIdOrderedList(new ArrayList<>(List.of(5L, 6L)))
			.build()
			.get();

		List<Folder> targetFolderList = new ArrayList<>();
		targetFolderList.add(folder1);
		List<Folder> userFolderList = List.of(folder1, folder2, folder3);

		given(folderDataHandler.getFolderList(any())).willReturn(targetFolderList);
		given(folderDataHandler.getFolderListByUserId(anyLong())).willReturn(userFolderList);

		// when
		folderService.deleteFolder(command);

		//then
		ArgumentCaptor<List<Long>> pickIdListCaptor = ArgumentCaptor.forClass(List.class);
		then(pickDataHandler).should(times(1)).movePickListToRecycleBin(anyLong(), pickIdListCaptor.capture());
		then(sharedFolderDataHandler).should(atLeast(1)).deleteBySourceFolderId(anyLong());
		then(folderDataHandler).should(times(1)).getFolderList(any());
		then(folderDataHandler).should(times(1)).getFolderListByUserId(any());
		then(folderDataHandler).should(times(1)).deleteFolderList(any());

		List<Long> targetPickIdList = pickIdListCaptor.getValue();
		assertThat(targetPickIdList).containsExactlyInAnyOrder(1L, 2L, 5L, 6L);
	}

	@Test
	@DisplayName("루트 폴더 리스트 조회")
	void root_folder_test() {
		// given
		Long userId = 1L;
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder rootFolder = FolderFixture
			.builder()
			.id(1L)
			.user(user)
			.folderType(FolderType.ROOT)
			.childFolderIdOrderedList(new ArrayList<>())
			.childPickIdOrderedList(new ArrayList<>())
			.build()
			.get();

		given(folderDataHandler.getRootFolder(any())).willReturn(rootFolder);

		// when
		folderService.getAllRootFolderList(userId);

		// then
		then(folderMapper).should(times(1)).toResult(rootFolder);
	}

	@Test
	@DisplayName("기본 폴더 리스트 조회")
	void basic_folder_test() {
		// given
		Long userId = 1L;
		User user = UserFixture
			.builder().id(userId).build().get();
		Folder root = FolderFixture
			.builder()
			.id(1L)
			.user(user)
			.folderType(FolderType.ROOT)
			.childFolderIdOrderedList(new ArrayList<>())
			.childPickIdOrderedList(new ArrayList<>())
			.build()
			.get();
		Folder unclassified = FolderFixture
			.builder()
			.id(2L)
			.user(user)
			.folderType(FolderType.GENERAL)
			.childFolderIdOrderedList(new ArrayList<>())
			.childPickIdOrderedList(new ArrayList<>())
			.build()
			.get();
		Folder recycleBin = FolderFixture
			.builder()
			.id(3L)
			.user(user)
			.folderType(FolderType.RECYCLE_BIN)
			.childFolderIdOrderedList(new ArrayList<>())
			.childPickIdOrderedList(new ArrayList<>())
			.build()
			.get();

		given(folderDataHandler.getRootFolder(any())).willReturn(root);
		given(folderDataHandler.getUnclassifiedFolder(any())).willReturn(unclassified);
		given(folderDataHandler.getRecycleBin(any())).willReturn(recycleBin);

		// when
		folderService.getBasicFolderList(userId);

		// then
		then(folderMapper).should(times(1)).toResult(root);
		then(folderMapper).should(times(1)).toResult(unclassified);
		then(folderMapper).should(times(1)).toResult(recycleBin);
	}
}