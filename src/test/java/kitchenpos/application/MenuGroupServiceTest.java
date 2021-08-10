package kitchenpos.application;

import static kitchenpos.application.fixture.MenuGroupFixture.MENU_GROUP1;
import static kitchenpos.application.fixture.MenuGroupFixture.MENU_GROUP1_REQUEST;
import static kitchenpos.application.fixture.MenuGroupFixture.MENU_GROUP2_REQUEST_SAME_NAME;
import static kitchenpos.application.fixture.MenuGroupFixture.MENU_GROUP2_SAME_NAME;
import static kitchenpos.application.fixture.MenuGroupFixture.MENU_GROUP_WITH_NAME_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;

class MenuGroupServiceTest extends MockTest {

    private static final int ZERO = 0;
    private static final int ONE = 1;

    @Mock
    private MenuGroupRepository menuGroupRepository;

    private MenuGroupService menuGroupService;

    @BeforeEach
    void setUp() {
        menuGroupService = new MenuGroupService(menuGroupRepository);
    }

    @DisplayName("create - 메뉴 그룹을 추가할 수 있다")
    @Test
    void createOK() {
        //given
        given(menuGroupRepository.save(any())).willReturn(MENU_GROUP1());

        //when
        final MenuGroup sut = menuGroupService.create(MENU_GROUP1_REQUEST());

        //then
        assertAll(
            () -> assertThat(sut.getId()).isNotNull(),
            () -> assertThat(sut.getName()).isEqualTo(MENU_GROUP1().getName())
        );
    }

    @DisplayName("create - 메뉴 그룹 이름이 한글자 이상이 아니라면 예외를 던진다")
    @ParameterizedTest
    @NullAndEmptySource
    void createWithEmptyName(final String value) {
        //given
        final MenuGroup menuGroup = MENU_GROUP_WITH_NAME_REQUEST(value);

        //when, then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> menuGroupService.create(menuGroup));
    }

    @DisplayName("create - 메뉴 그룹 이름은 중복될 수 있다")
    @Test
    void createWithDuplicateName() {
        //given
        given(menuGroupRepository.save(any())).willReturn(MENU_GROUP1())
            .willReturn(MENU_GROUP2_SAME_NAME());

        //when
        final MenuGroup sut1 = menuGroupService.create(MENU_GROUP1_REQUEST());
        final MenuGroup sut2 = menuGroupService.create(MENU_GROUP2_REQUEST_SAME_NAME());

        //then
        assertAll(
            () -> assertThat(sut1.getId()).isNotEqualTo(sut2.getId()),
            () -> assertThat(sut1.getName()).isEqualTo(sut2.getName())
        );
    }

    @DisplayName("findAll - 메뉴 그룹 리스트를 조회할 수 있다")
    @Test
    void findAll() {
        //given
        given(menuGroupRepository.findAll()).willReturn(Arrays.asList(MENU_GROUP1(), MENU_GROUP2_SAME_NAME()));

        //when
        final List<MenuGroup> menuGroups = menuGroupService.findAll();

        //then
        assertAll(
            () -> assertThat(menuGroups.get(ZERO).getId()).isNotEqualTo(menuGroups.get(ONE).getId()),
            () -> assertThat(MENU_GROUP1().getId()).isEqualTo(menuGroups.get(ZERO).getId()),
            () -> assertThat(MENU_GROUP2_SAME_NAME().getId()).isEqualTo(menuGroups.get(ONE).getId())
        );
    }

}
