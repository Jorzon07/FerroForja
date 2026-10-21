package co.ferroforja.bodega.folios.service;

import co.ferroforja.bodega.folios.dto.response.FolioTiqueteResponse;
import co.ferroforja.bodega.abastecimiento.entity.Abastecimiento;
import co.ferroforja.bodega.abastecimiento.repository.AbastecimientoRepository;
import co.ferroforja.bodega.restituciones.entity.Restitucion;
import co.ferroforja.bodega.restituciones.repository.RestitucionRepository;
import co.ferroforja.bodega.expendio.entity.Expendio;
import co.ferroforja.bodega.expendio.repository.ExpendioRepository;
import co.ferroforja.bodega.comun.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de generar las representaciones estructuradas
 * de comprobantes imprimibles para ventas, compras y devoluciones (RN-12).
 */
@Service
public class FolioService {

    private final ExpendioRepository expendioRepository;
    private final AbastecimientoRepository abastecimientoRepository;
    private final RestitucionRepository restitucionRepository;

    public FolioService(ExpendioRepository expendioRepository,
                           AbastecimientoRepository abastecimientoRepository,
                           RestitucionRepository restitucionRepository) {
        this.expendioRepository = expendioRepository;
        this.abastecimientoRepository = abastecimientoRepository;
        this.restitucionRepository = restitucionRepository;
    }

    @Transactional(readOnly = true)
    public FolioTiqueteResponse recuperarTiqueteExpendio(String correlativo) {
        Expendio v = expendioRepository.findByCorrelativo(correlativo)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante de venta no encontrado con consecutivo: " + correlativo));

        FolioTiqueteResponse doc = new FolioTiqueteResponse();
        doc.setTipoIdentificacion("EXPENDIO");
        doc.setCorrelativo(v.getCorrelativo());
        doc.setMarcaTiempo(v.getMarcaTiempo());
        doc.setSocioDenominacion(v.getComprador().getNombreLegal());
        doc.setSocioFolio(v.getComprador().getTipoIdentificacion() + " " + v.getComprador().getNroIdentificacion());
        doc.setSocioUbicacion(v.getComprador().getUbicacion());
        doc.setSocioCelular(v.getComprador().getCelular());
        doc.setSubtotal(v.getSubtotal());
        doc.setTotal(v.getTotal());
        doc.setImporteAbonado(v.getImporteAbonadoEfectivo());
        doc.setVuelto(v.getVueltoEntregado());
        doc.setSituacion(v.getSituacion());
        doc.setOperarioResponsable(v.getOperario() != null ? v.getOperario().getNombreVisible() : "SISTEMA");
        doc.setCausa(v.getCausaAnulacion());

        var detalles = v.getDetalles().stream().map(l -> new FolioTiqueteResponse.DetalleTiquete(
                l.getFicha().getCodigoEan(),
                l.getFicha().getNombreVisible(),
                l.getVolumen(),
                l.getFicha().getMedidaBase().getClave(),
                l.getTarifaIndividual(),
                l.getSubtotal()
        )).toList();
        doc.setDetalles(detalles);

        return doc;
    }

    @Transactional(readOnly = true)
    public FolioTiqueteResponse recuperarTiqueteAbastecimiento(String correlativo) {
        Abastecimiento c = abastecimientoRepository.findByCorrelativo(correlativo)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante de compra no encontrado con consecutivo: " + correlativo));

        FolioTiqueteResponse doc = new FolioTiqueteResponse();
        doc.setTipoIdentificacion("ABASTECIMIENTO");
        doc.setCorrelativo(c.getCorrelativo());
        doc.setMarcaTiempo(c.getMarcaTiempo());
        doc.setSocioDenominacion(c.getSuministrador().getNombreLegal());
        doc.setSocioFolio(c.getSuministrador().getTipoIdentificacion() + " " + c.getSuministrador().getNroIdentificacion());
        doc.setSocioUbicacion(c.getSuministrador().getUbicacion());
        doc.setSocioCelular(c.getSuministrador().getCelular());
        doc.setSubtotal(c.getTotal());
        doc.setTotal(c.getTotal());
        doc.setSituacion(c.getSituacion());
        doc.setOperarioResponsable(c.getOperario() != null ? c.getOperario().getNombreVisible() : "SISTEMA");
        doc.setCausa(c.getCausaAnulacion());

        var detalles = c.getDetalles().stream().map(l -> new FolioTiqueteResponse.DetalleTiquete(
                l.getFicha().getCodigoEan(),
                l.getFicha().getNombreVisible(),
                l.getVolumen(),
                l.getFicha().getMedidaBase().getClave(),
                l.getCosteIndividual(),
                l.getSubtotal()
        )).toList();
        doc.setDetalles(detalles);

        return doc;
    }

    @Transactional(readOnly = true)
    public FolioTiqueteResponse recuperarTiqueteRestitucion(String correlativo) {
        Restitucion d = restitucionRepository.findByCorrelativo(correlativo)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante de devolución no encontrado con consecutivo: " + correlativo));

        FolioTiqueteResponse doc = new FolioTiqueteResponse();
        doc.setTipoIdentificacion("RESTITUCION");
        doc.setCorrelativo(d.getCorrelativo());
        doc.setMarcaTiempo(d.getMarcaTiempo());
        if (d.getComprador() != null) {
            doc.setSocioDenominacion(d.getComprador().getNombreLegal());
            doc.setSocioFolio(d.getComprador().getTipoIdentificacion() + " " + d.getComprador().getNroIdentificacion());
            doc.setSocioUbicacion(d.getComprador().getUbicacion());
            doc.setSocioCelular(d.getComprador().getCelular());
        }
        doc.setSituacion(d.getSituacion());
        doc.setOperarioResponsable(d.getOperario() != null ? d.getOperario().getNombreVisible() : "SISTEMA");
        doc.setCausa(d.getCausa());

        var detalles = d.getDetalles().stream().map(l -> new FolioTiqueteResponse.DetalleTiquete(
                l.getFicha().getCodigoEan(),
                l.getFicha().getNombreVisible(),
                l.getVolumen(),
                l.getFicha().getMedidaBase().getClave(),
                l.getCosteIndividualAplicado(),
                l.getVolumen().multiply(l.getCosteIndividualAplicado())
        )).toList();
        doc.setDetalles(detalles);

        return doc;
    }
}
