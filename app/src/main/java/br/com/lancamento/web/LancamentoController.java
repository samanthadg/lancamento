package br.com.lancamento.web;

import br.com.lancamento.domain.Lancamento;
import br.com.lancamento.domain.Situacao;
import br.com.lancamento.domain.TipoLancamento;
import br.com.lancamento.repo.LancamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lancamentos")
public class LancamentoController {
  private static final Set<String> CAMPOS_ORDENACAO =
      Set.of("id", "descricao", "dataLancamento", "valor", "tipoLancamento", "situacao");

  private final LancamentoRepository lancamentoRepository;

  public LancamentoController(LancamentoRepository lancamentoRepository) {
    this.lancamentoRepository = lancamentoRepository;
  }

  @GetMapping
  public String listar(
      @RequestParam(defaultValue = "dataLancamento") String campo,
      @RequestParam(defaultValue = "desc") String direcao,
      Model model) {
    String campoOrdenacao = CAMPOS_ORDENACAO.contains(campo) ? campo : "dataLancamento";
    Sort.Direction direction = "asc".equalsIgnoreCase(direcao) ? Sort.Direction.ASC : Sort.Direction.DESC;
    var lista = lancamentoRepository.findAll(Sort.by(direction, campoOrdenacao).and(Sort.by("id")));
    model.addAttribute("lancamentos", lista);
    model.addAttribute("campo", campoOrdenacao);
    model.addAttribute("direcao", direction.name().toLowerCase());
    return "lancamentos/lista";
  }

  @PostMapping
  public String adicionar(
      @RequestParam String descricao,
      @RequestParam String dataLancamento,
      @RequestParam String valor,
      @RequestParam String tipoLancamento,
      @RequestParam String situacao,
      RedirectAttributes redirectAttributes) {
    try {
      Lancamento lancamento = new Lancamento();
      lancamento.setDescricao(descricao.trim());
      lancamento.setDataLancamento(LocalDate.parse(dataLancamento));
      lancamento.setValor(new BigDecimal(valor));
      lancamento.setTipoLancamento(TipoLancamento.valueOf(tipoLancamento));
      lancamento.setSituacao(Situacao.valueOf(situacao));
      lancamentoRepository.save(lancamento);
      redirectAttributes.addFlashAttribute("msg", "Lançamento adicionado com sucesso.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("erro", "Não foi possível adicionar o lançamento.");
    }
    return "redirect:/lancamentos";
  }

  @PostMapping("/{id}/excluir")
  public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    if (lancamentoRepository.existsById(id)) {
      lancamentoRepository.deleteById(id);
      redirectAttributes.addFlashAttribute("msg", "Lançamento excluído.");
    } else {
      redirectAttributes.addFlashAttribute("erro", "Lançamento não encontrado.");
    }
    return "redirect:/lancamentos";
  }
}

