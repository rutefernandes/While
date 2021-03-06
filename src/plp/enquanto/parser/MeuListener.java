package plp.enquanto.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import plp.enquanto.linguagem.Linguagem.*;

public class MeuListener extends EnquantoBaseListener {
	private final Leia leia = new Leia();
	private final Skip skip = new Skip();
	private final ParseTreeProperty<Object> values = new ParseTreeProperty<>();

	private Programa programa;

	public Programa getPrograma() {
		return programa;
	}

	private void setValue(final ParseTree node, final Object value) {
		values.put(node, value);
	}

	private Object getValue(final ParseTree node) {
		return values.get(node);
	}

	@Override
	public void exitBooleano(final EnquantoParser.BooleanoContext ctx) {
		setValue(ctx, new Booleano(ctx.getText().equals("verdadeiro")));
	}

	@Override
	public void exitLeia(final EnquantoParser.LeiaContext ctx) {
		setValue(ctx, leia);
	}
	
	@Override
	public void exitPara(final EnquantoParser.ParaContext ctx) {
		final Id id = new Id(ctx.ID().getText());
		final Expressao esq = (Expressao) getValue(ctx.expressao(0));
		final Expressao dir = (Expressao) getValue(ctx.expressao(1));
		final Comando comando = (Comando) getValue(ctx.comando());
		final Inteiro passo = ctx.INT() != null ? 
				new Inteiro(Integer.parseInt(ctx.INT().getText())) :
				new Inteiro(1);
		setValue(ctx, new Para(id, esq, dir, passo,comando));
	}
	
	@Override
	public void exitEscolha(final EnquantoParser.EscolhaContext ctx) {
		final Id id = new Id(ctx.ID().getText());
		final Map<Expressao, Comando> escolhas = new HashMap<Expressao, Comando>();
		for (int i = 0; i < ctx.expressao().size(); i++) {
			escolhas.put(
					(Expressao) getValue(ctx.expressao(i)),
					(Comando) getValue(ctx.comando(i))
					);
		}
		final Comando outro = (Comando) getValue(ctx.comando(ctx.comando().size()-1));
		setValue(ctx, new Escolha(id, escolhas, outro));
	}

	@Override
	public void exitSe(final EnquantoParser.SeContext ctx) {
		final Bool condicao = (Bool) getValue(ctx.bool(0));
		final Map<Bool, Comando> senaoses = new HashMap<Bool, Comando>();
		
		for (int i = 1; i < ctx.bool().size(); i++) {
			senaoses.put(
					(Bool) getValue(ctx.bool(i)),
					(Comando) getValue(ctx.comando(i))
					);
		}
		final Comando entao = (Comando) getValue(ctx.comando(0));
		final Comando senao = (Comando) getValue(ctx.comando(ctx.comando().size() - 1));
		setValue(ctx, new Se(condicao, entao, senao, senaoses));
	}
	
	@Override
	public void exitDefFuncao(final EnquantoParser.DefFuncaoContext ctx) {
		final Id nome = new Id(ctx.ID(0).getText());
		final List<Id> parametros = new ArrayList<Id>();
		for (int i = 1; i < ctx.ID().size(); i++) {
			parametros.add(new Id(ctx.ID(i).getText()));
		}
		final Expressao expressao = (Expressao) getValue(ctx.expressao());
		
		setValue(ctx, new DefFuncao(nome, parametros, expressao));
	}
	
	@Override
	public void exitChamadaFuncao(final EnquantoParser.ChamadaFuncaoContext ctx) {
		final Id nome = new Id(ctx.ID().getText());
		final List<Expressao> valores = new ArrayList<>();
		for (int i = 0; i < ctx.expressao().size(); i++) {
			valores.add((Expressao) getValue(ctx.expressao(i)));
		}
		
		setValue(ctx, new ChamadaFuncao(nome, valores));
	}

	@Override
	public void exitInteiro(final EnquantoParser.InteiroContext ctx) {
		setValue(ctx, new Inteiro(Integer.parseInt(ctx.getText())));
	}

	@Override
	public void exitSkip(final EnquantoParser.SkipContext ctx) {
		setValue(ctx, skip);
	}

	@Override
	public void exitEscreva(final EnquantoParser.EscrevaContext ctx) {
		final Expressao exp = (Expressao) getValue(ctx.expressao());
		setValue(ctx, new Escreva(exp));
	}

	@Override
	public void exitPrograma(final EnquantoParser.ProgramaContext ctx) {
		@SuppressWarnings("unchecked")
		final List<Comando> cmds = (List<Comando>) getValue(ctx.seqComando());
		programa = new Programa(cmds);
		setValue(ctx, programa);
	}

	@Override
	public void exitId(final EnquantoParser.IdContext ctx) {
		setValue(ctx, new Id(ctx.ID().getText()));
	}

	@Override
	public void exitSeqComando(final EnquantoParser.SeqComandoContext ctx) {
		final List<Comando> comandos = new ArrayList<>();
		for (EnquantoParser.ComandoContext c : ctx.comando()) {
			comandos.add((Comando) getValue(c));
		}
		setValue(ctx, comandos);
	}

	@Override
	public void exitAtribuicao(final EnquantoParser.AtribuicaoContext ctx) {
		final String id = ctx.ID().getText();
		final Expressao exp = (Expressao) getValue(ctx.expressao());
		setValue(ctx, new Atribuicao(id, exp));
	}

	@Override
	public void exitBloco(final EnquantoParser.BlocoContext ctx) {
		@SuppressWarnings("unchecked")
		final List<Comando> cmds = (List<Comando>) getValue(ctx.seqComando());
		setValue(ctx, new Bloco(cmds));
	}

	@Override
	public void exitOpBin(final EnquantoParser.OpBinContext ctx) {
		final Expressao esq = (Expressao) getValue(ctx.expressao(0));
		final Expressao dir = (Expressao) getValue(ctx.expressao(1));
		final String op = ctx.getChild(1).getText();
		final ExpBin exp;
		switch (op) {
		case "+":
			exp = new ExpSoma(esq, dir);
			break;
		case "*":
			exp = new ExpMult(esq, dir);
			break;
		case "-":
			exp = new ExpSub(esq, dir);
			break;
		case "/":
			exp = new ExpDiv(esq, dir);
			break;
		case "^":
			exp = new ExpPow(esq, dir);
			break;
		default:
			exp = new ExpSoma(esq, dir);
		}
		setValue(ctx, exp);
	}

	@Override
	public void exitEnquanto(final EnquantoParser.EnquantoContext ctx) {
		final Bool condicao = (Bool) getValue(ctx.bool());
		final Comando comando = (Comando) getValue(ctx.comando());
		setValue(ctx, new Enquanto(condicao, comando));
	}

	@Override
	public void exitELogico(final EnquantoParser.ELogicoContext ctx) {
		final Bool esq = (Bool) getValue(ctx.bool(0));
		final Bool dir = (Bool) getValue(ctx.bool(1));
		setValue(ctx, new ELogico(esq, dir));
	}

	@Override
	public void exitBoolPar(final EnquantoParser.BoolParContext ctx) {
		setValue(ctx, getValue(ctx.bool()));
	}

	@Override
	public void exitNaoLogico(final EnquantoParser.NaoLogicoContext ctx) {
		final Bool b = (Bool) getValue(ctx.bool());
		setValue(ctx, new NaoLogico(b));
	}

	@Override
	public void exitOuLogico(final EnquantoParser.OuLogicoContext ctx) {
		final Bool a = (Bool) getValue(ctx.bool(0));
		final Bool b = (Bool) getValue(ctx.bool(1));
		setValue(ctx, new OuLogico(a,b));
	}
	
	@Override
	public void exitXorLogico(final EnquantoParser.XorLogicoContext ctx) {
		final Bool a = (Bool) getValue(ctx.bool(0));
		final Bool b = (Bool) getValue(ctx.bool(1));
		setValue(ctx, new XorLogico(a,b));
	}
	
	@Override
	public void exitExpPar(final EnquantoParser.ExpParContext ctx) {
		setValue(ctx, getValue(ctx.expressao()));
	}

	@Override
	public void exitExiba(final EnquantoParser.ExibaContext ctx) {
		final String t = ctx.Texto().getText();
		final String texto = t.substring(1, t.length() - 1);
		setValue(ctx, new Exiba(texto));
	}

	@Override
	public void exitOpRel(final EnquantoParser.OpRelContext ctx) {
		final Expressao esq = (Expressao) getValue(ctx.expressao(0));
		final Expressao dir = (Expressao) getValue(ctx.expressao(1));
		final String op = ctx.getChild(1).getText();
		final ExpRel exp;
		switch (op) {
		case "=":
			exp = new ExpIgual(esq, dir);
			break;
		case "<=":
			exp = new ExpMenorIgual(esq, dir);
			break;
		case ">=":
			exp = new ExpMaiorIgual(esq, dir);
			break;
		case "<>":
			exp = new ExpDiferente(esq, dir);
			break;
		default:
			exp = new ExpIgual(esq, dir);
		}
		setValue(ctx, exp);
	}
}
