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

/**
 * [중요 !!! ]
 * 테스트 마다 데이터가 필요하다. 우리는 데이터 주입과 삭제에 대한 3가지 방법이 있다.
 * 1. @BeforeEach, @AfterEach
 * 2. sql
 * 3. @Transactional
 *
 * 현재 나는 @SpringBoot + @Transactional 방법으로 통일했다. 하나의 테스트마다 DB에 저장되더라도 @Transactional 가
 * 저장된 데이터를 롤백해준다.
 *
 * 하지만 이는 trade-off 가 있다. 실제 서비스 레이어 코드에는 @Transactional 을 까먹은 경우, 테스트 클래스에 @Transactional이
 * 있기에 실제로 DB에 CRUD 와 같은 작업이 동작한 것처럼 보인다.
 * @Transactional 은 이와 같은 장점이 있지만 실수, 버그를 유발할 수 있으므로 인지하고 사용하자.
 */