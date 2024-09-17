//package stocker.support;
//
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.CsvSource;
//
//class MarketStateTest {
//
//    @ParameterizedTest
//    @CsvSource({
//        "2024, 7, 25",
//        "2024, 2, 15",
//        "2024, 8, 2",
//    })
//    void isOpen(int year, int month, int dayOfMonth) {
//        LocalDate date = LocalDate.of(year, month, dayOfMonth);
//        assertTrue(MarketState.isOpen(date));
//    }
//
//    @ParameterizedTest
//    @CsvSource({
//            "2024, 12, 24",
//            "2024, 6, 6",
//            "2024, 4, 1",
//    })
//    void isClosed(int year, int month, int dayOfMonth) {
//        LocalDate date = LocalDate.of(year, month, dayOfMonth);
//        assertTrue(MarketState.isClosed(date));
//    }
//}