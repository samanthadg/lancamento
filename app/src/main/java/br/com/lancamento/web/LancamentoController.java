package br.com.lancamento.web;

import br.com.lancamento.repo.LancamentoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/lancamentos")
public class LancamentoController {
  private final LancamentoRepository lancamentoRepository;

  public LancamentoController(LancamentoRepository lancamentoRepository) {
    this.lancamentoRepository = lancamentoRepository;
  }

  @GetMapping
  public String listar(Model model) {
    var lista =
        lancamentoRepository.findAll(
            Sort.by(Sort.Direction.DESC, "dataLancamento").and(Sort.by("id")));
    model.addAttribute("lancamentos", lista);
    return "lancamentos/lista";
  }
}

