package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import kitchenpos.domain.OrderRepository;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.OrderTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class OrderTableServiceTest extends MockTest {

    @Mock
    private OrderTableRepository orderTableRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderTableService orderTableService;

    @BeforeEach
    public void setUp() {
        orderTableService = new OrderTableService(orderTableRepository, orderRepository);
    }

    @DisplayName("create - 주문 테이블을 추가할 수 있다")
    @Test
    void cteate() {
        //given
        final OrderTable orderTable = makeOrderTable("테이블1", 0, true);

        given(orderTableRepository.save(any())).willReturn(orderTable);

        //when
        final OrderTable sut = orderTableService.create(orderTable);

        //then
        assertThat(sut).isInstanceOf(OrderTable.class);
    }

    @DisplayName("create - 주문 테이블 이름이 한글자 미만이라면 예외를 반환한다")
    @ParameterizedTest
    @NullAndEmptySource
    void cteateWithEmptyName(final String name) {
        //given
        final OrderTable orderTable = makeOrderTable(name, 0, true);

        //when, then
        assertThatThrownBy(
            () -> orderTableService.create(orderTable)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("create - 주문 테이블 이름은 중복될 수 있다")
    @ParameterizedTest
    @ValueSource(strings = {"테이블1", "테이블2", "테이블3"})
    void cteateWithDuplicateName(final String name) {
        //given
        final OrderTable orderTable1 = makeOrderTable(name, 0, true);
        final OrderTable orderTable2 = makeOrderTable(name, 1, false);

        given(orderTableRepository.save(any())).willReturn(orderTable1, orderTable2);

        //when
        final OrderTable createdOrderTable1 = orderTableService.create(orderTable1);
        final OrderTable createdOrderTable2 = orderTableService.create(orderTable2);

        //then
        assertThat(createdOrderTable1.getName()).isEqualTo(createdOrderTable2.getName());
    }

    @DisplayName("sit - 주문 테이블에 손님이 앉을 수 있다")
    @Test
    void sit() {
        //given
        final OrderTable orderTable = makeOrderTable("테이블1", 0, true);

        given(orderTableRepository.findById(any())).willReturn(Optional.of(orderTable));

        //when
        final OrderTable sut = orderTableService.sit(orderTable.getId());

        //then
        assertThat(sut.isEmpty()).isFalse();
    }

    @DisplayName("sit - 테이블이 존재하지 않는다면 예외를 반환한다")
    @Test
    void sitWIthNotExistTable() {
        //given
        final OrderTable orderTable = makeOrderTable("테이블1", 0, true);

        given(orderTableRepository.findById(any())).willThrow(NoSuchElementException.class);

        //when, then
        assertThatThrownBy(
            () -> orderTableService.sit(orderTable.getId())
        ).isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("clear - 주문 테이블을 치울 수 있다")
    @Test
    void clear() {
        //given
        final OrderTable orderTable = makeOrderTable("테이블1", 0, false);

        given(orderTableRepository.findById(any())).willReturn(Optional.of(orderTable));

        //when
        final OrderTable sut = orderTableService.clear(orderTable.getId());

        //then
        assertThat(sut.isEmpty()).isTrue();
    }

    @DisplayName("clear - 테이블이 존재하지 않는다면 예외를 반환한다")
    @Test
    void clearWIthNotExistTable() {
        //given
        final OrderTable orderTable = makeOrderTable("테이블1", 0, true);

        given(orderTableRepository.findById(any())).willThrow(NoSuchElementException.class);

        //when, then
        assertThatThrownBy(
            () -> orderTableService.clear(orderTable.getId())
        ).isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("clear - 테이블의 주문 상태가 모두 완료가 아닐 경우 예외를 반환한다")
    @Test
    void clearStatus() {
        //given
        final OrderTable orderTable = makeOrderTable("테이블1", 0, true);

        given(orderTableRepository.findById(any())).willReturn(Optional.of(orderTable));
        given(
            orderRepository.existsByOrderTableAndStatusNot(orderTable, OrderStatus.COMPLETED)
        ).willThrow(IllegalArgumentException.class);

        //when, then
        assertThatThrownBy(
            () -> orderTableService.clear(orderTable.getId())
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("changeGuestNumber - 테이블의 앉은 손님의 수를 변경할 수 있다")
    @Test
    void changeGuestNumber() {
        //given
        final OrderTable orderTable = makeOrderTable("테이블1", 0, false);

        given(orderTableRepository.findById(any())).willReturn(Optional.of(orderTable));

        orderTable.setNumberOfGuests(4);

        //when
        final OrderTable sut = orderTableService.changeNumberOfGuests(orderTable.getId(), orderTable);

        //then
        assertThat(sut).isInstanceOf(OrderTable.class);
    }

    @DisplayName("changeGuestNumber - 변경할 손님의 수가 음수라면 예외를 반환한다")
    @Test
    void changeGuestNumberNegativeNumber() {
        //given
        final OrderTable orderTable = makeOrderTable("테이블1", 0, false);

        orderTable.setNumberOfGuests(-1);

        //when, then
        assertThatThrownBy(
            () -> orderTableService.changeNumberOfGuests(orderTable.getId(), orderTable)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("changeGuestNumber - 존재하는 테이블이 아니라면 예외를 반환한다")
    @Test
    void changeGuestNumberNotExistTable() {
        //given
        final OrderTable orderTable = makeOrderTable("테이블1", 0, false);

        given(orderTableRepository.findById(any())).willThrow(NoSuchElementException.class);

        //when, then
        assertThatThrownBy(
            () -> orderTableService.changeNumberOfGuests(orderTable.getId(), orderTable)
        ).isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("changeGuestNumberStatus - 테이블이 비어있는 경우 예외를 반환한다")
    @Test
    void changeGuestNumberStatus() {
        //given
        final OrderTable orderTable = makeOrderTable("테이블1", 0, true);

        given(orderTableRepository.findById(any())).willReturn(Optional.of(orderTable));

        //when, then
        assertThatThrownBy(
            () -> orderTableService.changeNumberOfGuests(orderTable.getId(), orderTable)
        ).isInstanceOf(IllegalStateException.class);
    }

    @DisplayName("findAll - 테이블 리스트를 조회할 수 있다")
    @Test
    void findAll() {
        //given
        final OrderTable orderTable1 = makeOrderTable("테이블1", 0, true);
        final OrderTable orderTable2 = makeOrderTable("테이블2", 0, true);

        given(orderTableRepository.findAll()).willReturn(Arrays.asList(orderTable1, orderTable2));

        //when
        final List<OrderTable> sut = orderTableService.findAll();

        //then
        assertAll(
            () -> assertThat(sut.get(0)).isEqualTo(orderTable1),
            () -> assertThat(sut.get(1)).isEqualTo(orderTable2)
        );
    }

    private OrderTable makeOrderTable(final String name, final int numberOfGuests, final boolean isEmpty) {
        final OrderTable orderTable = new OrderTable();
        orderTable.setId(UUID.randomUUID());
        orderTable.setName(name);
        orderTable.setNumberOfGuests(numberOfGuests);
        orderTable.setEmpty(isEmpty);
        return orderTable;
    }
}