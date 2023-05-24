package br.com.Controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.DAO.MarcacoesFeitasDAO;
import br.com.Entity.MarcacoesFeitas;

@WebServlet("/MarcacoesFeitasServlet")
public class MarcacoesFeitasServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private MarcacoesFeitasDAO marcacoesFeitasDAO; 
    //private CalculoAtrasoDAO trabalho;   

    public void init() {
        marcacoesFeitasDAO = new MarcacoesFeitasDAO();
        //trabalho = new CalculoAtrasoDAO();         
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {      	
		String action = request.getParameter("action");		
    	try {
			switch (action) {
			case "add":
				adicionarMarcacao(request, response);				
				break;		
			default:
				listarMarcacoes(request, response);
				break;
			}
		} catch (Exception e) {
			// trata a exceção
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("Ocorreu um erro ao processar a solicitação: " + e.getMessage());
		}    	
       
    }
    
    private void adicionarMarcacao(HttpServletRequest request, HttpServletResponse response) throws Exception {    	
    	String cpf = (String) request.getSession().getAttribute("cpf");//recebo cpf por sessão para saber em qual cpf marcar as horasre
    	request.getSession().setAttribute("cpf", cpf);// insiro o cpf no request para ser usado no método listar     	
    	String entrada = request.getParameter("entrada");
        String intervaloInicio = request.getParameter("intervaloInicio");
        String intervaloFim = request.getParameter("intervaloFim");
        String saida = request.getParameter("saida");        
       
        MarcacoesFeitas horario = new MarcacoesFeitas();
        horario.setCpf(cpf);
        horario.setEntrada(entrada);
        horario.setIntervaloInicio(intervaloInicio);
        horario.setIntervaloFim(intervaloFim);
        horario.setSaida(saida);
        
        if(entrada == null || entrada.isEmpty()) {                   
        	MarcacoesFeitas resgate = new MarcacoesFeitas();
        	resgate = marcacoesFeitasDAO.buscarUltimaEntrada();
        	horario.setId(resgate.getId());
        	horario.setEntrada(resgate.getEntrada());           	
          
           	CalculoAtrasoServlet calculoAtrasoServlet = new CalculoAtrasoServlet();       
           	calculoAtrasoServlet.adicionarAtraso(horario);      
        }else { 
        	// se entrada estiver presente ele apenas salva a entrada
        	 marcacoesFeitasDAO.salvar(horario);        
        }        
        listarMarcacoes(request, response);        
}
    
    private void removerMarcacao(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {       
    	String cpf = request.getParameter("cpf");
        marcacoesFeitasDAO.excluirPorCPF(cpf);
        listarMarcacoes(request, response);
    }

    private void listarMarcacoes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	String cpf = (String) request.getSession().getAttribute("cpf");// recebo cpf por sessão para saber de qual cpf listar as marcaçoes    	 
        List<MarcacoesFeitas> marcacoes = marcacoesFeitasDAO.listarTodosPorCpf(cpf);
        request.setAttribute("marcacoes", marcacoes);
        request.getRequestDispatcher("controleDeHora.jsp").forward(request, response);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");        	
        if (action != null) {
            switch (action) {
                case "delete":
                	removerMarcacao(request, response);
                    break;
                case "list":                	
                    break;
                
                default:
                	listarMarcacoes(request, response);
                    break;
            }
        } else {
        	listarMarcacoes(request, response);
        }
    }
}
