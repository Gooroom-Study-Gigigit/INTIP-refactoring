package module;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public abstract class IntegrationSupportTest {

    /*@MockBean
    protected WebClientUtil webClientUtil;*/
}

/**
 * 하나의 스프링 부트 서버를 사용하여 스프링 빈이 관련된 테스트를 진행합니다.
 * 저의 생각으로는 외부 API에 대한 테스트는 우리 손을 떠난 외부 세계이기도 하면서 매번 테스트 진행 시,
 * 실제로 요청을 보내는 것도 비용이기에 외부 API 사용과 같은 외부 세계에 대한 객체, 빈은 @MockBean 을 통해
 * Mocking 하겠습니다.
 *
 * 참고로 @DataJpaTest 에 관한 테스트 모듈을 따로 만들어 분리해도 됩니다. 저는 스프링 서버 생성을 최소화 하기 위해
 * 컨트롤러, 통합 테스트(서비스, 리포지토리 레이어) 를 기준으로 2개가 발생되게 했습니다.
 *
 *
 * [사용 방법]
 * 위 외부와 소통하기 위한 빈, 객체는 @MockBean 을 넣어 사용하면 됩니다.
 * 해당 클래스 모듈을 기준으로 테스트가 수행되므로 부모 객체에 대해 고민하면 사용하시면 됩니다.
 */
