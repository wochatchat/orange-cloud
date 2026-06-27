//
//  DeveloperHubView.swift
//  Orange Cloud
//
//  「开发者平台」Tab：把 Workers 与各开发者平台资源收进一个分类入口，
//  对齐 Cloudflare 的产品分组（计算 / 数据与消息 / AI）。Workers 不再单独占一个 Tab。
//

import SwiftUI

struct DeveloperHubView: View {

    let session: SessionStore

    var body: some View {
        NavigationStack {
            List {
                Section("计算") {
                    // Workers 免费（按 scope 门控，不走 Pro）
                    PermissionGatedNavigationLink(
                        label: "Workers", systemImage: "bolt.fill",
                        requiredScope: "workers-scripts.read", showsChevron: true
                    ) {
                        WorkerListView(session: session)
                            .id(session.selectedAccount?.id)
                    }
                    ProGatedNavigationLink(
                        label: "Cloudflare Pages", systemImage: "doc.richtext",
                        requiredScope: "page.read", feature: .pages, showsChevron: true
                    ) { PagesProjectListView(session: session) }
                }
                .glassRow()

                Section("数据与消息") {
                    ProGatedNavigationLink(
                        label: "Queues", systemImage: "tray.2",
                        requiredScope: "queues.read", feature: .queues, showsChevron: true
                    ) { QueuesView(session: session) }
                    ProGatedNavigationLink(
                        label: "Durable Objects", systemImage: "cube.transparent",
                        requiredScope: "workers-scripts.read", feature: .durableObjects, showsChevron: true
                    ) { DurableObjectsView(session: session) }
                    ProGatedNavigationLink(
                        label: "Hyperdrive", systemImage: "bolt.horizontal.circle",
                        requiredScope: "query-cache.read", feature: .hyperdrive, showsChevron: true
                    ) { HyperdriveView(session: session) }
                }
                .glassRow()

                Section("AI") {
                    ProGatedNavigationLink(
                        label: "Workers AI", systemImage: "brain",
                        requiredScope: "ai.read", feature: .workersAI, showsChevron: true
                    ) { WorkersAIView(session: session) }
                    ProGatedNavigationLink(
                        label: "AI Gateway", systemImage: "brain.head.profile",
                        requiredScope: "aig.read", feature: .aiGateway, showsChevron: true
                    ) { AIGatewayView(session: session) }
                }
                .glassRow()
            }
            .daybreakList()
            .background { SkyBackground() }
            .navigationTitle("开发者平台")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
