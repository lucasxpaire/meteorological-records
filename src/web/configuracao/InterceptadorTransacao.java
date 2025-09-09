package web.configuracao;

import dados.Dados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class InterceptadorTransacao extends HandlerInterceptorAdapter {

    @Autowired
    Dados dados;

    @Override
    public boolean preHandle(HttpServletRequest requisicao, HttpServletResponse resposta, Object lidar) {
        dados.iniciarTransacao();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest requisicao, HttpServletResponse resposta, Object lidar, Exception excecao) {
        if (excecao == null) {
            dados.confirmarTransacao();
        } else {
            dados.desfazerTransacao();
        }
    }

}
